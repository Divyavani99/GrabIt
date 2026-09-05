import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { placeOrder } from "../api/orders";
import Loader from "../components/Loader";
import "../styles/checkout.css";

const PAYMENT_METHODS = [
  { id: "UPI", label: "UPI" },
  { id: "CARD", label: "Credit / debit card" },
  { id: "WALLET", label: "Wallet" },
  { id: "COD", label: "Cash on delivery" },
];

export default function Checkout() {
  const { userId, profile, refreshProfile } = useAuth();
  const { cart, refreshCart } = useCart();
  const navigate = useNavigate();

  const [addressId, setAddressId] = useState(profile?.addresses?.[0]?.id ?? "");
  const [paymentMethod, setPaymentMethod] = useState("UPI");
  const [placing, setPlacing] = useState(false);
  const [error, setError] = useState(null);

  if (!cart) return <Loader label="Loading" />;

  const addresses = profile?.addresses ?? [];

  const handlePlaceOrder = async () => {
    if (!addressId) {
      setError("Add a delivery address first.");
      return;
    }
    setPlacing(true);
    setError(null);
    try {
      const { orderId } = await placeOrder(userId, {
        cartId: userId,
        addressId,
        paymentMethod,
        promoCode: cart.promoCode || undefined,
      });
      await refreshCart();
      await refreshProfile();
      navigate(`/orders/${orderId}`);
    } catch (err) {
      setError(err.message);
    } finally {
      setPlacing(false);
    }
  };

  return (
    <div className="container checkout-page">
      <h1 style={{ marginBottom: 24 }}>Checkout</h1>

      {error && <div className="error-banner">{error}</div>}

      <section className="checkout-section">
        <h2>Deliver to</h2>
        {addresses.length === 0 ? (
          <p className="muted">
            No saved addresses yet.{" "}
            <button className="checkout-link" onClick={() => navigate("/addresses")}>
              Add one
            </button>
            .
          </p>
        ) : (
          <div className="checkout-options">
            {addresses.map((a) => (
              <label key={a.id} className="checkout-option">
                <input
                  type="radio"
                  name="address"
                  checked={addressId === a.id}
                  onChange={() => setAddressId(a.id)}
                />
                <span>
                  <strong>{a.label || "Address"}</strong> — {a.street}, {a.city}
                </span>
              </label>
            ))}
          </div>
        )}
      </section>

      <section className="checkout-section">
        <h2>Pay with</h2>
        <div className="checkout-options">
          {PAYMENT_METHODS.map((m) => (
            <label key={m.id} className="checkout-option">
              <input
                type="radio"
                name="payment"
                checked={paymentMethod === m.id}
                onChange={() => setPaymentMethod(m.id)}
              />
              <span>{m.label}</span>
            </label>
          ))}
        </div>
      </section>

      <section className="checkout-section">
        <h2>Order summary</h2>
        <div className="checkout-summary">
          <div className="checkout-summary__row">
            <span className="muted">Subtotal</span>
            <span>₹{Number(cart.subtotal).toFixed(2)}</span>
          </div>
          {cart.promoCode && (
            <div className="checkout-summary__row">
              <span className="muted">Promo</span>
              <span>{cart.promoCode}</span>
            </div>
          )}
        </div>
      </section>

      <button
        className="btn btn-accent"
        style={{ width: "100%" }}
        disabled={placing || addresses.length === 0}
        onClick={handlePlaceOrder}
      >
        {placing ? "Placing order…" : "Place order"}
      </button>
    </div>
  );
}
