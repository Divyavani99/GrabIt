import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { removeCartItem, applyPromo } from "../api/cart";
import QuantityStepper from "../components/QuantityStepper";
import Loader from "../components/Loader";
import "../styles/cart.css";

export default function Cart() {
  const { userId } = useAuth();
  const { cart, refreshCart } = useCart();
  const navigate = useNavigate();
  const [promoInput, setPromoInput] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);

  if (cart === null) return <Loader label="Loading your cart" />;

  const items = cart.items ?? [];
  const deliveryFeeEstimate = 2.99;

  const handleRemove = async (cartItemId) => {
    setBusy(true);
    setError(null);
    try {
      await removeCartItem(userId, cartItemId);
      await refreshCart();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const handlePromo = async (e) => {
    e.preventDefault();
    if (!promoInput.trim()) return;
    setBusy(true);
    setError(null);
    try {
      await applyPromo(userId, promoInput.trim());
      await refreshCart();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="container cart-page">
      <h1 style={{ marginBottom: 24 }}>Your cart</h1>

      {error && <div className="error-banner">{error}</div>}

      {items.length === 0 ? (
        <div>
          <p className="muted">Your cart is empty.</p>
          <button className="btn btn-secondary" onClick={() => navigate("/")}>
            Browse restaurants
          </button>
        </div>
      ) : (
        <>
          <div className="cart-receipt">
            {items.map((item) => (
              <div key={item.id} className="cart-receipt__row">
                <div>
                  <p className="cart-receipt__name">{item.itemName}</p>
                  {item.customizations && <p className="cart-receipt__custom">{item.customizations}</p>}
                </div>
                <div className="cart-receipt__right">
                  <span className="cart-receipt__price">
                    ₹{(item.unitPrice * item.quantity).toFixed(2)}
                  </span>
                  <button className="cart-receipt__remove" disabled={busy} onClick={() => handleRemove(item.id)}>
                    Remove
                  </button>
                </div>
              </div>
            ))}

            <hr className="hairline" style={{ margin: "12px 0" }} />

            <form className="cart-promo" onSubmit={handlePromo}>
              <input
                placeholder="Promo code"
                value={promoInput}
                onChange={(e) => setPromoInput(e.target.value)}
              />
              <button className="btn btn-secondary" type="submit" disabled={busy}>
                Apply
              </button>
            </form>
            {cart.promoCode && (
              <p className="cart-receipt__applied">Applied: {cart.promoCode}</p>
            )}

            <hr className="hairline" style={{ margin: "12px 0" }} />

            <div className="cart-receipt__row">
              <span className="muted">Subtotal</span>
              <span>₹{Number(cart.subtotal).toFixed(2)}</span>
            </div>
            <div className="cart-receipt__row">
              <span className="muted">Delivery fee (estimate)</span>
              <span>₹{deliveryFeeEstimate.toFixed(2)}</span>
            </div>
          </div>

          <button
            className="btn btn-accent"
            style={{ width: "100%", marginTop: 24 }}
            onClick={() => navigate("/checkout")}
          >
            Go to checkout
          </button>
        </>
      )}
    </div>
  );
}
