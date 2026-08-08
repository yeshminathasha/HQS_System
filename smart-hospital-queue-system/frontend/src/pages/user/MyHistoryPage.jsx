import { useEffect, useState } from "react";
import axiosClient from "../../api/axiosClient";
import { useAuth } from "../../auth/AuthContext";

export default function MyHistoryPage() {
  const { user } = useAuth();
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    axiosClient.get("/patients/history").then(r => {
      // Client-side filter to this user's own records if there's no
      // dedicated backend filter yet (linkedUserId set at registration/booking time)
      const mine = r.data.filter(p => p.linkedUserId === user.id);
      setHistory(mine);
    }).finally(() => setLoading(false));
  }, [user.id]);

  if (loading) return <p>Loading...</p>;

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">My History</h1>
      <div className="bg-white rounded-lg shadow divide-y">
        {history.length === 0 && <p className="p-4 text-gray-500">No past visits yet.</p>}
        {history.map(p => (
          <div key={p.id} className="p-4">
            <p className="font-medium">{p.doctorName}</p>
            <p className="text-sm text-gray-500">{new Date(p.registeredAt).toLocaleString()} — {p.status}</p>
          </div>
        ))}
      </div>
    </div>
  );
}