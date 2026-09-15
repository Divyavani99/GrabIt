
import {
  createContext,
  useContext,
  useEffect,
  useState,
  useCallback,
} from "react";
import { registerUser, getProfile } from "../api/users";

const AuthContext = createContext(null);

const STORAGE_KEY = "food-delivery.auth";

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  });

  const [profile, setProfile] = useState(null);
  const [loadingProfile, setLoadingProfile] = useState(false);

  const refreshProfile = useCallback(async () => {
    if (!auth?.userId || !auth?.token) return;

    setLoadingProfile(true);

    try {
      const data = await getProfile(auth.userId, auth.token);
      setProfile(data);
    } catch (error) {
      console.error("Failed to load profile:", error);

      // If the token is invalid/expired, clear authentication.
      if (error.status === 401 || error.status === 403) {
        setAuth(null);
        setProfile(null);
      }
    } finally {
      setLoadingProfile(false);
    }
  }, [auth?.userId, auth?.token]);

  useEffect(() => {
    if (auth) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }, [auth]);

  useEffect(() => {
    refreshProfile();
  }, [refreshProfile]);

  const register = async (form) => {
    const { userId, token } = await registerUser(form);

    setAuth({
      userId,
      token,
      name: form.name,
      email: form.email,
    });

    return userId;
  };

  const signOut = () => {
    setAuth(null);
    setProfile(null);
  };

  return (
    <AuthContext.Provider
      value={{
        userId: auth?.userId ?? null,
        token: auth?.token ?? null,
        name: auth?.name ?? profile?.name ?? null,
        profile,
        loadingProfile,
        isAuthenticated: Boolean(auth?.userId && auth?.token),
        register,
        signOut,
        refreshProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);

  if (!ctx) {
    throw new Error("useAuth must be used within AuthProvider");
  }

  return ctx;
}

