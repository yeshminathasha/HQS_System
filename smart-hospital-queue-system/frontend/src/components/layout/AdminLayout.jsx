import { Outlet } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";

export default function AdminLayout() {
  const { logout, user } = useAuth();
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className="bg-white shadow px-6 py-4 flex justify-between items-center">
        <h1 className="text-xl font-bold text-gray-800">Admin Portal</h1>
        <div className="flex items-center space-x-4">
          <span className="text-sm text-gray-600">Welcome, {user?.name}</span>
          <button onClick={logout} className="text-sm text-red-600 hover:text-red-800">Logout</button>
        </div>
      </header>
      <main className="flex-1 p-6">
        <Outlet />
      </main>
    </div>
  );
}
