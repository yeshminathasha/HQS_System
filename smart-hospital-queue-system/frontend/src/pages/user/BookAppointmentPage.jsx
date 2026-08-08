import { useEffect, useState } from "react";
import axiosClient from "../../api/axiosClient";
import { bookForSelf } from "../../api/appointmentApi";

export default function BookAppointmentPage() {
  const [doctors, setDoctors] = useState([]);
  const [form, setForm] = useState({ doctorId: "", dateTime: "", reason: "" });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    axiosClient.get("/doctors").then(r => setDoctors(r.data));
    // Pre-fill with the least-loaded doctor as a suggestion
    axiosClient.get("/doctors/recommend").then(r => {
      if (r.data?.id) setForm(f => ({ ...f, doctorId: r.data.id }));
    });
  }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess(false);
    try {
      await bookForSelf(form);
      setSuccess(true);
      setForm({ doctorId: "", dateTime: "", reason: "" });
    } catch (err) {
      // 409 = slot conflict/double-booking, surface it inline
      setError(err.response?.data?.message || "Could not book appointment");
    }
  };

  return (
    <div className="max-w-lg space-y-4">
      <h1 className="text-2xl font-semibold">Book an appointment</h1>
      {success && <p className="text-green-600">Appointment booked!</p>}
      {error && <p className="text-red-600">{error}</p>}
      <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow p-4 space-y-3">
        <select name="doctorId" required value={form.doctorId} onChange={handleChange}
          className="w-full border rounded px-3 py-2">
          <option value="">Select a doctor</option>
          {doctors.map(d => (
            <option key={d.id} value={d.id}>{d.name} — {d.department}</option>
          ))}
        </select>
        <input type="datetime-local" name="dateTime" required value={form.dateTime}
          onChange={handleChange} className="w-full border rounded px-3 py-2" />
        <textarea name="reason" placeholder="Reason for visit" value={form.reason}
          onChange={handleChange} className="w-full border rounded px-3 py-2" />
        <button type="submit" className="bg-blue-600 text-white rounded px-4 py-2">
          Book
        </button>
      </form>
    </div>
  );
}