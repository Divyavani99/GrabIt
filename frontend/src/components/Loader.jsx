export default function Loader({ label = "Loading" }) {
  return (
    <div style={{ padding: "40px 0", textAlign: "center", color: "var(--muted)" }}>
      {label}…
    </div>
  );
}
