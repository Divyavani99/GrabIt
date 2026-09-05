import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getOrder } from "../api/orders";
import { useOrderTracking } from "../hooks/useOrderTracking";
import StatusTimeline from "../components/StatusTimeline";
import Loader from "../components/Loader";
import "../styles/order-tracking.css";

const STATUS_LABELS = {
  ASSIGNED: "Driver assigned",
  HEADED_TO_RESTAURANT: "Heading to the restaurant",
  PICKED_UP: "Picked up your order",
  EN_ROUTE: "On the way",
  ARRIVED: "Arrived",
  DELIVERED: "Delivered",
};

export default function OrderTracking() {
  const { orderId } = useParams();
  const [order, setOrder] = useState(null);
  const [error, setError] = useState(null);
  const { tracking, connected } = useOrderTracking(orderId);

  useEffect(() => {
    let cancelled = false;
    const load = () => {
      getOrder(orderId)
        .then((data) => !cancelled && setOrder(data))
        .catch((err) => !cancelled && setError(err.message));
    };
    load();
    // Order status also changes asynchronously (payment/delivery events), so
    // poll it independently of the tracking WebSocket.
    const interval = setInterval(load, 6000);
    return () => {
      cancelled = true;
      clearInterval(interval);
    };
  }, [orderId]);

  if (error) return <div className="container"><div className="error-banner">{error}</div></div>;
  if (!order) return <Loader label="Loading your order" />;

  return (
    <div className="container tracking-page">
      <h1 style={{ marginBottom: 4 }}>Order #{order.id.slice(0, 8)}</h1>
      <p className="muted" style={{ marginBottom: 32 }}>
        Placed {new Date(order.createdAt).toLocaleString()}
      </p>

      <StatusTimeline status={order.status} />

      <section className="tracking-card">
        <h2>Delivery</h2>
        {order.deliveryPartnerName ? (
          <>
            <p>
              <strong>{order.deliveryPartnerName}</strong> is on the way.
            </p>
            {tracking ? (
              <div className="tracking-live">
                <p>{STATUS_LABELS[tracking.status] ?? tracking.status}</p>
                {tracking.etaMinutes != null && <p className="muted">ETA: {tracking.etaMinutes} min</p>}
                <p className="muted">
                  Last position: {tracking.lat.toFixed(4)}, {tracking.lng.toFixed(4)}
                </p>
                <p className="tracking-live__connection">
                  {connected ? "Live" : "Reconnecting…"}
                </p>
              </div>
            ) : (
              <p className="muted">Waiting for the first location update…</p>
            )}
          </>
        ) : (
          <p className="muted">Looking for a nearby delivery partner…</p>
        )}
      </section>

      <section className="tracking-card">
        <h2>Payment</h2>
        <p>
          Status: <strong>{order.paymentStatus}</strong> · Method: {order.paymentMethod}
        </p>
      </section>

      <section className="tracking-card">
        <h2>Items</h2>
        {order.items?.map((item) => (
          <div key={item.id} className="tracking-item">
            <span>
              {item.quantity} × {item.itemName}
            </span>
            <span>₹{(item.unitPrice * item.quantity).toFixed(2)}</span>
          </div>
        ))}
        <hr className="hairline" style={{ margin: "12px 0" }} />
        <div className="tracking-item">
          <span className="muted">Subtotal</span>
          <span>₹{Number(order.subtotal).toFixed(2)}</span>
        </div>
        {Number(order.discount) > 0 && (
          <div className="tracking-item">
            <span className="muted">Discount</span>
            <span>−₹{Number(order.discount).toFixed(2)}</span>
          </div>
        )}
        <div className="tracking-item">
          <span className="muted">Delivery fee</span>
          <span>₹{Number(order.deliveryFee).toFixed(2)}</span>
        </div>
        <div className="tracking-item tracking-item--total">
          <span>Total</span>
          <span>₹{Number(order.total).toFixed(2)}</span>
        </div>
      </section>
    </div>
  );
}
