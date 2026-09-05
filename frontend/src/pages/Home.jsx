import { useEffect, useMemo, useState } from "react";
import RestaurantRow from "../components/RestaurantRow";
import Loader from "../components/Loader";
import { getNearbyRestaurants, searchRestaurants } from "../api/restaurants";
import "../styles/home.css";

// Default coordinates used until/unless the browser grants geolocation —
// Hyderabad, so the demo has somewhere sensible to start from.
const DEFAULT_COORDS = { lat: 17.385, lng: 78.4867 };

const CUISINES = ["All", "Indian", "Italian", "Chinese", "Mexican", "Desserts"];

export default function Home() {
  const [coords, setCoords] = useState(DEFAULT_COORDS);
  const [query, setQuery] = useState("");
  const [cuisine, setCuisine] = useState("All");
  const [sortBy, setSortBy] = useState("distance");
  const [restaurants, setRestaurants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!navigator.geolocation) return;
    navigator.geolocation.getCurrentPosition(
      (pos) => setCoords({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
      () => {
        /* keep the default coords if the user declines */
      },
      { timeout: 4000 }
    );
  }, []);

  const isSearching = query.trim().length > 0 || cuisine !== "All";

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    const fetcher = isSearching
      ? searchRestaurants({
          query: query.trim() || undefined,
          lat: coords.lat,
          lng: coords.lng,
          cuisine: cuisine === "All" ? undefined : cuisine,
          sortBy,
        })
      : getNearbyRestaurants({ lat: coords.lat, lng: coords.lng, radius: 8 });

    fetcher
      .then((data) => {
        if (!cancelled) setRestaurants(data ?? []);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [coords, query, cuisine, sortBy, isSearching]);

  const sorted = useMemo(() => restaurants, [restaurants]);

  return (
    <div className="container home">
      <h1 className="home__headline">What are you craving?</h1>

      <input
        className="home__search"
        placeholder="Search restaurants or dishes"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />

      <div className="home__filters">
        <div className="home__cuisines">
          {CUISINES.map((c) => (
            <button
              key={c}
              className={`home__chip ${cuisine === c ? "home__chip--active" : ""}`}
              onClick={() => setCuisine(c)}
              type="button"
            >
              {c}
            </button>
          ))}
        </div>

        <select value={sortBy} onChange={(e) => setSortBy(e.target.value)} className="home__sort">
          <option value="distance">Nearest first</option>
          <option value="rating">Top rated</option>
        </select>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {loading ? (
        <Loader label="Finding restaurants near you" />
      ) : sorted.length === 0 ? (
        <p className="muted" style={{ padding: "40px 0" }}>
          Nothing turned up here. Try a different search or cuisine.
        </p>
      ) : (
        <div>
          {sorted.map((r) => (
            <RestaurantRow key={r.id} restaurant={r} />
          ))}
        </div>
      )}
    </div>
  );
}
