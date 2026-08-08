import { useEffect, useState } from "react";
import { getMyAppointments } from "../../api/appointmentApi";

export default function UserDashboardPage() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getMyAppointments()
      .then(setAppointments)
      .finally(() => setLoading(false));
  }, []);

  const next = appointments
    .filter(a => a.status === "SCHEDULED")
    .sort((a, b) => new Date(a.dateTime) - new Date(b.dateTime))[0];

  if (loading) return <p>Loading...</p>;

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">My Dashboard</h1>
      <div className="bg-white rounded-lg shadow p-4">
        <h2 className="font-medium mb-2">Next appointment</h2>
        {next ? (
          <p>{next.doctorName} — {new Date(next.dateTime).toLocaleString()}</p>
        ) : (
          <p className="text-gray-500">No upcoming appointments.</p>
        )}
      </div>
    </div>
  );
}