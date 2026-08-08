import { useEffect, useState } from "react";
import { getMyAppointments, cancelOwn } from "../../api/appointmentApi";

export default function MyAppointmentsPage() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = () => getMyAppointments().then(setAppointments).finally(() => setLoading(false));

  useEffect(() => { load(); }, []);

  const handleCancel = async (id) => {
    if (!confirm("Cancel this appointment?")) return;
    await cancelOwn(id);
    load();
  };

  if (loading) return <p>Loading...</p>;

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">My Appointments</h1>
      <div className="bg-white rounded-lg shadow divide-y">
        {appointments.length === 0 && <p className="p-4 text-gray-500">No appointments yet.</p>}
        {appointments.map(a => (
          <div key={a.id} className="p-4 flex justify-between items-center">
            <div>
              <p className="font-medium">{a.doctorName}</p>
              <p className="text-sm text-gray-500">{new Date(a.dateTime).toLocaleString()} — {a.status}</p>
            </div>
            {a.status === "SCHEDULED" && (
              <button onClick={() => handleCancel(a.id)} className="text-red-600 text-sm">
                Cancel
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}