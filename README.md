# Food Delivery Platform — Spring Boot Microservices

A Swiggy/Zomato-style food delivery backend built as independent Spring Boot
microservices, communicating over REST (sync) and Kafka (async), registered
with Eureka and fronted by a Spring Cloud Gateway.

## Entity → service mapping

| # | Entity            | Owning service      | Notes |
|---|--------------------|----------------------|-------|
| 1 | **User**           | `user-service`       | `order_history[]` is populated asynchronously by consuming `order-events` |
| 2 | **Restaurant**     | `restaurant-service`  | includes `deliveryTime` |
| 3 | **FoodMenu**       | `restaurant-service`  | `item_id, restaurant_id, name, price, image_url, category, customizations[]` |
| 4 | **Cart**           | `order-service`       | `cart_id, user_id, items[], total_amount (derived), promo_code` |
| 5 | **Order**          | `order-service`       | tracks `status`, `payment_status`, `delivery_partner_id`, timestamps; updated asynchronously from `payment-events` and `delivery-events` |
| 6 | **DeliveryPartner**| `delivery-service` *(new)* | `driver_id, name, phone, vehicle, current_location, availability_status, assigned_orders[]` |
| 7 | **Payment**        | `payment-service` *(new)* | `payment_id, order_id, amount, method, status, gateway_response` |

## Services

| Service              | Port | Responsibility                                              | Datastore        |
|-----------------------|------|---------------------------------------------------------------|-------------------|
| `eureka-server`       | 8761 | Service discovery                                              | —                 |
| `api-gateway`         | 8080 | Single entry point, routes `/v1/**` to the right service       | —                 |
| `user-service`        | 8081 | Registration, JWT, profile, addresses, order history            | PostgreSQL        |
| `restaurant-service`  | 8082 | Restaurant/FoodMenu catalog, nearby + search                    | PostgreSQL (+Caffeine cache) |
| `order-service`       | 8083 | Cart, checkout, order lifecycle & status aggregation             | PostgreSQL        |
| `tracking-service`    | 8084 | Real-time order/driver tracking (WebSocket) + last-known lookup | Redis             |
| `delivery-service`    | 8085 | Driver onboarding, availability, order assignment, location pings | PostgreSQL      |
| `payment-service`     | 8086 | Payment processing (simulated PSP), payment history              | PostgreSQL      |

## Kafka topics & event flow

```
order-service        --publishes-->  order-events           (OrderPlacedEvent)
                      --publishes-->  order-status-events    (OrderStatusChangedEvent)

user-service          --consumes--    order-events           -> appends orderId to User.orderHistory
payment-service       --consumes--    order-events           -> auto-charges the order
delivery-service      --consumes--    order-events           -> assigns an available driver

payment-service       --publishes-->  payment-events         (PaymentCompletedEvent)
order-service         --consumes--    payment-events         -> sets Order.paymentStatus / status

delivery-service      --publishes-->  delivery-events        (DeliveryAssignedEvent)
order-service         --consumes--    delivery-events        -> sets Order.deliveryPartnerId/Name

delivery-service       --publishes-->  delivery-tracking      (DeliveryTrackingUpdate, on every driver ping)
tracking-service        --consumes--    delivery-tracking      -> caches in Redis + fans out over WebSocket
```

All events are keyed by `orderId` so per-order ordering is preserved within a partition.

## API surface (via the gateway, `http://localhost:8080`)

```
POST   /v1/users/register
GET    /v1/users/{userId}
POST   /v1/users/{userId}/addresses

GET    /v1/restaurants/nearby?lat=&lng=&radius=
GET    /v1/restaurants/search?query=&lat=&lng=&cuisine=&rating=&sortBy=
GET    /v1/restaurants/{id}
GET    /v1/restaurants/{id}/menu?page=&size=

GET    /v1/cart                       (header: X-User-Id)
POST   /v1/cart/items?restaurantId=   (header: X-User-Id)
DELETE /v1/cart/items/{itemId}        (header: X-User-Id)
POST   /v1/cart/promo                 (header: X-User-Id)  — apply a promo code
POST   /v1/orders                     (header: X-User-Id)
GET    /v1/orders/{orderId}

WS     /v1/orders/{orderId}/track
GET    /v1/delivery/{orderId}/tracking

POST   /v1/delivery-partners                              — onboard a driver
GET    /v1/delivery-partners/{id}
PATCH  /v1/delivery-partners/{id}/availability
POST   /v1/delivery-partners/{id}/orders/{orderId}/location — driver app pushes a GPS ping

POST   /v1/payments                    — manual/retry charge
GET    /v1/payments/{paymentId}
GET    /v1/payments/order/{orderId}
```

> In a production gateway, `X-User-Id` would be stamped by a `GlobalFilter`
> that validates the JWT and injects the subject — that filter is omitted
> here for brevity; `user-service`'s `JwtAuthFilter`/`JwtUtil` show the token
> shape it expects.

## Architecture notes

- **Cart → Order flow**: `order-service` calls `restaurant-service` over HTTP
  (load-balanced `WebClient`, via Eureka) to re-price/validate items when they're
  added to the cart, so prices in the cart always reflect the live menu.
- **Order lifecycle via Kafka, not synchronous calls**: placing an order publishes
  `OrderPlacedEvent`; `payment-service` and `delivery-service` react independently
  (charge the order / assign a driver) and publish their own outcomes back onto
  Kafka, which `order-service` consumes to update `paymentStatus`, `status`, and
  `deliveryPartnerId` — no service blocks on another's response.
- **Real-time tracking**: `delivery-service` publishes a `DeliveryTrackingUpdate` on
  every driver location ping. `tracking-service` consumes it, caches the latest
  update per order in Redis (`tracking:current:{orderId}`, 6h TTL), and republishes
  it on a Redis pub/sub channel. Every `tracking-service` instance subscribes to
  `tracking:*`, so a WebSocket client can connect to *any* instance behind the load
  balancer and still receive updates — this is what lets the WebSocket tier scale
  horizontally.
- **Distance search**: `restaurant-service` pre-filters with a SQL bounding box, then
  computes exact Haversine distance and sorts in the service layer — no PostGIS
  dependency needed for this scale.
- **Driver assignment**: `delivery-service` picks the first `AVAILABLE` partner on
  each `OrderPlacedEvent`; a production system would rank by proximity to the
  restaurant and current load instead.
- **Payments**: `payment-service` auto-charges every order via a simulated gateway
  client (`PaymentGatewayClient`, ~95% success rate) so the SUCCESS/FAILED paths in
  `order-service` are both exercised without a real PSP integration.

## Running locally

```bash
docker compose up --build
```

This brings up Zookeeper, Kafka (+ Kafka UI on :8090), Redis, five Postgres
instances (one per persistent service), Eureka, the gateway, and all six
business services.

Example flow:

```bash
# Register
curl -X POST localhost:8080/v1/users/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Asha","email":"asha@example.com","phone":"+911234567890","password":"hunter22"}'

# Onboard a delivery partner
curl -X POST localhost:8080/v1/delivery-partners \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ravi","phone":"+919876543210","vehicle":"Bike","lat":17.385,"lng":78.4867}'

# Nearby restaurants
curl "localhost:8080/v1/restaurants/nearby?lat=17.385&lng=78.4867&radius=5"

# Add to cart
curl -X POST "localhost:8080/v1/cart/items?restaurantId=<id>" \
  -H "X-User-Id: <userId>" -H 'Content-Type: application/json' \
  -d '{"itemId":"<menuItemId>","quantity":2,"customizations":"no onions"}'

# Place order — this kicks off payment-service + delivery-service via Kafka
curl -X POST localhost:8080/v1/orders \
  -H "X-User-Id: <userId>" -H 'Content-Type: application/json' \
  -d '{"cartId":"<userId>","addressId":"<addressId>","paymentMethod":"UPI"}'

# Poll the order to see status/paymentStatus/deliveryPartner populate asynchronously
curl localhost:8080/v1/orders/<orderId>

# Track (WebSocket)
wscat -c ws://localhost:8080/v1/orders/<orderId>/track
```

## What's intentionally left out

- A real payment gateway integration (payment-service simulates one)
- Auth on the gateway itself (JWT validation shown in user-service only)
- Rate limiting, circuit breakers (Resilience4j), centralized config server, distributed tracing
- Proximity-ranked driver assignment (currently first-available)
