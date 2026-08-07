import { createContext, useContext, useState, useEffect } from "react";
import { authApi } from "../services/authApi";
import { getStoredToken, setStoredToken, clearStoredToken } from "../services/api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);   // { id, name, email }
  const [role, setRole] = useState(null);   // "ADMIN" | "USER"
  const [token, setToken] = useState(getStoredToken());
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const existing = getStoredToken();
    if (!existing) {
      setLoading(false);
      return;
    }
    authApi.getMe()
      .then((me) => {
        setUser(me);
        setRole(me.role);
        setToken(existing);
      })
      .catch(() => {
        clearStoredToken();
      })
      .finally(() => setLoading(false));
  }, []);

  const login = async (email, password) => {
    const res = await authApi.login(email, password);
    setStoredToken(res.token);
    setToken(res.token);
    setUser({ id: res.userId, name: res.name, email });
    setRole(res.role);
    return res;
  };

  const register = async (name, email, password) => {
    const res = await authApi.register(name, email, password);
    setStoredToken(res.token);
    setToken(res.token);
    setUser({ id: res.userId, name: res.name, email });
    setRole(res.role);
    return res;
  };

  const logout = () => {
    clearStoredToken();
    setToken(null);
    setUser(null);
    setRole(null);
  };

  return (
    <AuthContext.Provider value={{ user, role, token, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
