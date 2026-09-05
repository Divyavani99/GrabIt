import { createContext, useContext, useEffect, useState, useCallback } from "react";
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
    if (!auth?.userId) return;
    setLoadingProfile(true);
    try {
      const data = await getProfile(auth.userId);
      setProfile(data);
    } finally {
      setLoadingProfile(false);
    }
  }, [auth?.userId]);

  useEffect(() => {
    if (auth) localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
    else localStorage.removeItem(STORAGE_KEY);
  }, [auth]);

  useEffect(() => {
    refreshProfile();
  }, [refreshProfile]);

  const register = async (form) => {
    const { userId, token } = await registerUser(form);
    setAuth({ userId, token, name: form.name, email: form.email });
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
        isAuthenticated: Boolean(auth?.userId),
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
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
