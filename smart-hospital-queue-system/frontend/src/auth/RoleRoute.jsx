import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "./AuthContext";

export default function RoleRoute({ role }) {
  const { role: userRole } = useAuth();

  if (userRole !== role) return <Navigate to="/unauthorized" replace />;

  return <Outlet />;
}
