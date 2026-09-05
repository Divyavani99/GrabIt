import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getRestaurant, getMenu } from "../api/restaurants";
import { addCartItem } from "../api/cart";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import Loader from "../components/Loader";
import "../styles/restaurant-detail.css";

export default function RestaurantDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { userId, isAuthenticated } = useAuth();
  const { refreshCart } = useCart();

  const [restaurant, setRestaurant] = useState(null);
  const [menu, setMenu] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [addingId, setAddingId] = useState(null);
  const [notice, setNotice] = useState(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    Promise.all([getRestaurant(id), getMenu(id)])
      .then(([r, m]) => {
        if (cancelled) return;
        setRestaurant(r);
        setMenu(m);
      })
      .catch((err) => !cancelled && setError(err.message))
      .finally(() => !cancelled && setLoading(false));
    return () => {
      cancelled = true;
    };
  }, [id]);

  const handleAdd = async (item) => {
    if (!isAuthenticated) {
      navigate("/register");
      return;
    }
    setAddingId(item.id);
    setNotice(null);
    try {
      await addCartItem(userId, id, { itemId: item.id, quantity: 1 });
      await refreshCart();
      setNotice(`Added ${item.name} to your cart.`);
    } catch (err) {
      setNotice(err.message);
    } finally {
      setAddingId(null);
    }
  };

  if (loading) return <Loader label="Loading menu" />;
  if (error) return <div className="container"><div className="error-banner">{error}</div></div>;
  if (!restaurant) return null;

  const categories = Object.entries(menu?.itemsByCategory ?? {});

  return (
    <div className="container restaurant-detail">
      <div className="restaurant-detail__header">
        <h1>{restaurant.name}</h1>
        <p className="muted">
          {restaurant.cuisine} · {restaurant.rating?.toFixed(1)} rating · {restaurant.deliveryTime} min delivery
        </p>
        {!restaurant.open && <p className="restaurant-detail__closed">Currently closed</p>}
      </div>

      {notice && <div className="restaurant-detail__notice">{notice}</div>}

      {categories.length === 0 && <p className="muted">This restaurant hasn't published a menu yet.</p>}

      {categories.map(([category, items]) => (
        <section key={category} className="restaurant-detail__section">
          <h2 className="restaurant-detail__category">{category}</h2>
          <hr className="hairline" style={{ marginBottom: 4 }} />
          {items.map((item) => (
            <div key={item.id} className="menu-item">
              {item.imageUrl && <img className="menu-item__image" src={item.imageUrl} alt="" />}
              <div className="menu-item__body">
                <div className="menu-item__title-row">
                  <h3 className="menu-item__name">
                    {item.vegetarian && <span className="menu-item__veg" aria-label="Vegetarian" />}
                    {item.name}
                  </h3>
                  <span className="menu-item__price">₹{Number(item.price).toFixed(2)}</span>
                </div>
                {item.description && <p className="menu-item__description">{item.description}</p>}
                {item.customizations?.length > 0 && (
                  <p className="menu-item__customizations">
                    Customizable: {item.customizations.join(", ")}
                  </p>
                )}
                <button
                  className="btn btn-secondary menu-item__add"
                  disabled={!item.available || addingId === item.id}
                  onClick={() => handleAdd(item)}
                >
                  {!item.available ? "Unavailable" : addingId === item.id ? "Adding…" : "Add to cart"}
                </button>
              </div>
            </div>
          ))}
        </section>
      ))}
    </div>
  );
}
