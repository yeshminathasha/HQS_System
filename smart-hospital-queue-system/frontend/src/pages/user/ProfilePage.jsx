import { useAuth } from "../../auth/AuthContext";

export default function ProfilePage() {
  const { user, role } = useAuth();

  return (
    <div className="max-w-md space-y-4">
      <h1 className="text-2xl font-semibold">Profile</h1>
      <div className="bg-white rounded-lg shadow p-4 space-y-2">
        <p><span className="text-gray-500">Name:</span> {user?.name}</p>
        <p><span className="text-gray-500">Email:</span> {user?.email}</p>
        <p><span className="text-gray-500">Role:</span> {role}</p>
      </div>
    </div>
  );
}