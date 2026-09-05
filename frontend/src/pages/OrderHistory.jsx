import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { getOrder } from "../api/orders";
import Loader from "../components/Loader";
import "../styles/order-history.css";

export default function OrderHistory() {
  const { profile, loadingProfile } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const ids = profile?.orderHistory ?? [];
    if (ids.length === 0) {
      setOrders([]);
      setLoading(false);
      return;
    }
    setLoading(true);
    Promise.all(ids.map((id) => getOrder(id).catch(() => null)))
      .then((results) => setOrders(results.filter(Boolean).reverse()))
      .finally(() => setLoading(false));
  }, [profile]);

  if (loadingProfile || loading) return <Loader label="Loading your orders" />;

  return (
    <div className="container order-history">
      <h1 style={{ marginBottom: 24 }}>Your orders</h1>

      {orders.length === 0 ? (
        <p className="muted">You haven't placed any orders yet.</p>
      ) : (
        orders.map((order) => (
          <Link to={`/orders/${order.id}`} key={order.id} className="order-history__row">
            <div>
              <p className="order-history__id">Order #{order.id.slice(0, 8)}</p>
              <p className="muted">{new Date(order.createdAt).toLocaleString()}</p>
            </div>
            <div className="order-history__right">
              <span className={`order-history__status order-history__status--${order.status.toLowerCase()}`}>
                {order.status.replaceAll("_", " ").toLowerCase()}
              </span>
              <span className="order-history__total">₹{Number(order.total).toFixed(2)}</span>
            </div>
          </Link>
        ))
      )}
    </div>
  );
}
