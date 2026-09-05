import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import "../styles/header.css";

export default function Header() {
  const { isAuthenticated, name, signOut } = useAuth();
  const { itemCount } = useCart();
  const navigate = useNavigate();

  return (
    <header className="site-header">
      <div className="container site-header__row">
        <Link to="/" className="site-header__brand">
          Nearby
        </Link>

        {isAuthenticated && (
          <nav className="site-header__nav">
            <Link to="/orders">Orders</Link>
            <Link to="/addresses">Addresses</Link>
            <Link to="/cart" className="site-header__cart">
              Cart{itemCount > 0 && <span className="site-header__badge">{itemCount}</span>}
            </Link>
            <button
              className="site-header__signout"
              onClick={() => {
                signOut();
                navigate("/register");
              }}
            >
              {name ? `Sign out (${name.split(" ")[0]})` : "Sign out"}
            </button>
          </nav>
        )}
      </div>
    </header>
  );
}
