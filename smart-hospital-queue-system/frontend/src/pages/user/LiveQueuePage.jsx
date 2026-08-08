import { usePolling } from "../../hooks/usePolling";
import axiosClient from "../../api/axiosClient";

export default function LiveQueuePage() {
  const { data: queue, loading } = usePolling(
    () => axiosClient.get("/patients/queue").then(r => r.data),
    10000 // 10s, same interval as the admin queue view
  );

  if (loading) return <p>Loading...</p>;

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">Live Queue</h1>
      <table className="w-full bg-white rounded-lg shadow">
        <thead>
          <tr className="text-left border-b">
            <th className="p-3">Position</th>
            <th className="p-3">Patient</th>
            <th className="p-3">Doctor</th>
            <th className="p-3">Status</th>
          </tr>
        </thead>
        <tbody>
          {(queue || []).map((p, i) => (
            <tr key={p.id} className="border-b last:border-0">
              <td className="p-3">{i + 1}</td>
              <td className="p-3">{p.name}</td>
              <td className="p-3">{p.doctorName}</td>
              <td className="p-3">{p.status}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}