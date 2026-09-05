import { Link } from "react-router-dom";
import "../styles/restaurant-row.css";

const INITIAL_COLORS = ["#f2a900", "#c1440e", "#3f7d58", "#6b5b95", "#2f6690"];

function colorFor(name) {
  const idx = name.charCodeAt(0) % INITIAL_COLORS.length;
  return INITIAL_COLORS[idx];
}

export default function RestaurantRow({ restaurant }) {
  const { id, name, cuisine, rating, isOpen, distanceKm } = restaurant;

  return (
    <Link to={`/restaurants/${id}`} className="restaurant-row">
      <div className="restaurant-row__initial" style={{ background: colorFor(name) }}>
        {name.charAt(0).toUpperCase()}
      </div>
      <div className="restaurant-row__body">
        <h3 className="restaurant-row__name">{name}</h3>
        <p className="restaurant-row__meta">
          {cuisine} · {rating?.toFixed(1) ?? "—"} rating
          {distanceKm != null && ` · ${distanceKm.toFixed(1)} km away`}
        </p>
      </div>
      <div className="restaurant-row__status">
        {isOpen ? <span className="restaurant-row__open">Open</span> : <span className="muted">Closed</span>}
      </div>
    </Link>
  );
}
