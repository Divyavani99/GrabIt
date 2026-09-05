# Frontend — Nearby (food delivery)

A React (Vite) single-page app that talks to the platform entirely through
`api-gateway`. Covers the full customer journey: register → browse restaurants
→ build a cart → check out → live-track the order.

## Design

A "market menu-board" look rather than a generic SaaS template: warm paper
background, ink-black type, a single marigold accent for calls to action and
prices, chili reserved for alerts, leaf green for veg markers/confirmed
states. Restaurant and menu listings are hairline-divided rows (like a menu
board) instead of uniform shadowed cards. Headlines and prices use Fraunces
(serif, some personality); UI text uses Work Sans.

## Pages

| Route | Page | Notes |
|---|---|---|
| `/register` | Register | Backend only exposes `register`, no separate login — the session (`userId` + JWT) persists in `localStorage`, so re-registering isn't needed on return visits from the same browser |
| `/` | Home | Nearby restaurants (via geolocation, falls back to a default location) or search, with cuisine chips + sort |
| `/restaurants/:id` | Restaurant detail | Menu grouped by category, add to cart |
| `/cart` | Cart | Line items, promo code, remove items |
| `/checkout` | Checkout | Pick a saved address + payment method, place order |
| `/orders/:orderId` | Order tracking | Status timeline, **live WebSocket** driver location (`WS /v1/orders/{orderId}/track`), payment status, item breakdown |
| `/orders` | Order history | Backed by `User.orderHistory[]` on the profile |
| `/addresses` | Addresses | Add a delivery address (with a "use my location" shortcut) |

## Running it

```bash
npm install
cp .env.example .env   # point VITE_API_BASE_URL at your gateway if not localhost:8080
npm run dev
```

Opens on `http://localhost:5173`. Make sure `api-gateway` (and the rest of the
platform) is running — see the root `README.md` / `docker-compose.yml`.

Via Docker (already wired into the root `docker-compose.yml`, serves on
`http://localhost:3000`):

```bash
docker compose up --build frontend
```

## Notes on the live tracking

`useOrderTracking` (in `src/hooks`) opens a WebSocket to `tracking-service`
(via the gateway) and updates the page as `DeliveryTrackingUpdate` events
arrive. If the socket can't connect — e.g. no driver has been assigned yet —
it transparently falls back to polling `GET /v1/delivery/{orderId}/tracking`
every 5 seconds, so the page still works without a live socket.

## Known gaps (left for a v2)

- No real login/session refresh — only register + persisted local session
- No payment-method-on-file UI (backend has `User.paymentMethods[]`, unused here)
- No restaurant images (backend supports `FoodMenu.imageUrl`; restaurant-level imagery isn't modeled yet)
- No client-side form validation library — relies on native HTML5 `required`/`type` validation
