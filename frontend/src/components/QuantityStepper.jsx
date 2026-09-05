import "../styles/quantity-stepper.css";

export default function QuantityStepper({ quantity, onIncrement, onDecrement, disabled }) {
  return (
    <div className="qty-stepper">
      <button type="button" onClick={onDecrement} disabled={disabled || quantity <= 0} aria-label="Decrease quantity">
        −
      </button>
      <span>{quantity}</span>
      <button type="button" onClick={onIncrement} disabled={disabled} aria-label="Increase quantity">
        +
      </button>
    </div>
  );
}
