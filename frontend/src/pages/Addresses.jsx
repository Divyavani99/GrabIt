import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { addAddress } from "../api/users";
import Loader from "../components/Loader";
import "../styles/addresses.css";

export default function Addresses() {
  const { userId, profile, loadingProfile, refreshProfile } = useAuth();
  const [form, setForm] = useState({ label: "Home", street: "", city: "", lat: "", lng: "" });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const update = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));

  const useCurrentLocation = () => {
    if (!navigator.geolocation) return;
    navigator.geolocation.getCurrentPosition((pos) => {
      setForm((f) => ({
        ...f,
        lat: pos.coords.latitude.toFixed(6),
        lng: pos.coords.longitude.toFixed(6),
      }));
    });
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await addAddress(userId, {
        street: form.street,
        city: form.city,
        lat: parseFloat(form.lat),
        lng: parseFloat(form.lng),
        label: form.label,
      });
      await refreshProfile();
      setForm({ label: "Home", street: "", city: "", lat: "", lng: "" });
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (loadingProfile) return <Loader label="Loading addresses" />;

  return (
    <div className="container addresses-page">
      <h1 style={{ marginBottom: 24 }}>Your addresses</h1>

      <div className="addresses-list">
        {(profile?.addresses ?? []).length === 0 && <p className="muted">No addresses saved yet.</p>}
        {(profile?.addresses ?? []).map((a) => (
          <div key={a.id} className="addresses-list__item">
            <strong>{a.label || "Address"}</strong>
            <p className="muted">
              {a.street}, {a.city}
            </p>
          </div>
        ))}
      </div>

      <h2 style={{ fontSize: 18, marginTop: 32, marginBottom: 16 }}>Add a new address</h2>

      {error && <div className="error-banner">{error}</div>}

      <form onSubmit={onSubmit} className="addresses-form">
        <div className="field">
          <label htmlFor="label">Label</label>
          <input id="label" value={form.label} onChange={update("label")} placeholder="Home, Work…" />
        </div>
        <div className="field">
          <label htmlFor="street">Street</label>
          <input id="street" required value={form.street} onChange={update("street")} />
        </div>
        <div className="field">
          <label htmlFor="city">City</label>
          <input id="city" required value={form.city} onChange={update("city")} />
        </div>
        <div className="addresses-form__coords">
          <div className="field">
            <label htmlFor="lat">Latitude</label>
            <input id="lat" required type="number" step="any" value={form.lat} onChange={update("lat")} />
          </div>
          <div className="field">
            <label htmlFor="lng">Longitude</label>
            <input id="lng" required type="number" step="any" value={form.lng} onChange={update("lng")} />
          </div>
        </div>
        <button type="button" className="checkout-link" onClick={useCurrentLocation} style={{ marginBottom: 16 }}>
          Use my current location
        </button>
        <button className="btn btn-accent" type="submit" disabled={submitting} style={{ width: "100%" }}>
          {submitting ? "Saving…" : "Save address"}
        </button>
      </form>
    </div>
  );
}
