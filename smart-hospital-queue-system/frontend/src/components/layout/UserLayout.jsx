import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";

const links = [
  { to: "/app", label: "Dashboard", end: true },
  { to: "/app/book", label: "Book Appointment" },
  { to: "/app/my-appointments", label: "My Appointments" },
  { to: "/app/queue", label: "Live Queue" },
  { to: "/app/history", label: "My History" },
  { to: "/app/reports", label: "Reports" },
  { to: "/app/profile", label: "Profile" },
];

export default function UserLayout() {
  const { user, logout } = useAuth();

  return (
    <div className="flex min-h-screen">
      <aside className="w-56 bg-gray-900 text-white flex flex-col">
        <div className="p-4 font-semibold border-b border-gray-700">
          {user?.name || "My Account"}
        </div>
        <nav className="flex-1 p-2 space-y-1">
          {links.map((l) => (
            <NavLink
              key={l.to}
              to={l.to}
              end={l.end}
              className={({ isActive }) =>
                `block px-3 py-2 rounded text-sm ${isActive ? "bg-blue-600" : "hover:bg-gray-800"}`
              }
            >
              {l.label}
            </NavLink>
          ))}
        </nav>
        <button onClick={logout} className="p-3 text-sm text-left hover:bg-gray-800 border-t border-gray-700">
          Log out
        </button>
      </aside>
      <main className="flex-1 bg-gray-50 p-6">
        <Outlet />
      </main>
    </div>
  );
}