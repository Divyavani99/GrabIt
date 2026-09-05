import "../styles/status-timeline.css";

const STAGES = ["PLACED", "CONFIRMED", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED"];

const LABELS = {
  PLACED: "Placed",
  CONFIRMED: "Confirmed",
  PREPARING: "Preparing",
  READY_FOR_PICKUP: "Ready for pickup",
  OUT_FOR_DELIVERY: "Out for delivery",
  DELIVERED: "Delivered",
  CANCELLED: "Cancelled",
};

export default function StatusTimeline({ status }) {
  if (status === "CANCELLED") {
    return (
      <div className="status-timeline status-timeline--cancelled">
        <span className="status-timeline__dot status-timeline__dot--cancelled" />
        Order cancelled
      </div>
    );
  }

  const currentIndex = STAGES.indexOf(status === "READY_FOR_PICKUP" ? "PREPARING" : status);

  return (
    <ol className="status-timeline">
      {STAGES.map((stage, idx) => {
        const reached = idx <= currentIndex;
        return (
          <li key={stage} className={`status-timeline__step ${reached ? "status-timeline__step--reached" : ""}`}>
            <span className="status-timeline__dot" />
            <span className="status-timeline__label">{LABELS[stage]}</span>
          </li>
        );
      })}
    </ol>
  );
}
