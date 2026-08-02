import React, { useState } from 'react';
import { Card, CardHeader } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { CalendarPlus, X } from 'lucide-react';
import { appointmentService, doctorService } from '../services/api';
import { usePolling } from '../hooks/usePolling';

const STATUS_STYLES = {
  SCHEDULED: { label: 'Scheduled', variant: 'warning' },
  COMPLETED: { label: 'Completed', variant: 'success' },
  CANCELLED: { label: 'Cancelled', variant: 'neutral' },
};

export default function Appointments() {
  const [showModal, setShowModal] = useState(false);
  const [upcomingOnly, setUpcomingOnly] = useState(true);
  const [form, setForm] = useState({
    patientId: '',
    doctorName: '',
    department: 'General',
    appointmentDate: '',
    appointmentTime: '',
  });
  const [error, setError] = useState(null);

  const { data: appointments, loading, error: listError, refresh } = usePolling(
    () => appointmentService.getAppointments({ upcoming: upcomingOnly }).then((r) => r.data),
    15000,
    [upcomingOnly]
  );
  const { data: doctors } = usePolling(() => doctorService.getDoctors().then((r) => r.data), 60000);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      await appointmentService.createAppointment(form);
      setShowModal(false);
      setForm({ patientId: '', doctorName: '', department: 'General', appointmentDate: '', appointmentTime: '' });
      refresh();
    } catch (err) {
      setError(err.displayMessage || 'Failed to book appointment');
    }
  };

  const handleStatus = async (appointment, status) => {
    try {
      await appointmentService.updateStatus(appointment.id, status);
      refresh();
    } catch (err) {
      alert(err.displayMessage || 'Failed to update appointment');
    }
  };

  const badge = (status) => {
    const style = STATUS_STYLES[status] || STATUS_STYLES.SCHEDULED;
    return <Badge variant={style.variant}>{style.label}</Badge>;
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Appointments</h1>
          <p className="text-gray-500 text-sm mt-1">Book and manage patient appointments.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          <CalendarPlus className="h-4 w-4 mr-2" />
          New Appointment
        </button>
      </div>

      <Card>
        <div className="flex items-center justify-between mb-4 border-b border-gray-100 pb-4">
          <div className="flex items-center gap-2">
            <button
              onClick={() => setUpcomingOnly(true)}
              className={`px-3 py-1.5 rounded-md text-sm font-medium ${upcomingOnly ? 'bg-primary-50 text-primary-700' : 'text-gray-500 hover:bg-gray-50'}`}
            >
              Upcoming
            </button>
            <button
              onClick={() => setUpcomingOnly(false)}
              className={`px-3 py-1.5 rounded-md text-sm font-medium ${!upcomingOnly ? 'bg-primary-50 text-primary-700' : 'text-gray-500 hover:bg-gray-50'}`}
            >
              All
            </button>
          </div>
          {listError && <span className="text-xs text-danger-600">{listError.displayMessage}</span>}
        </div>

        {loading ? (
          <div className="p-8 text-center text-gray-500">Loading appointments...</div>
        ) : (appointments || []).length === 0 ? (
          <div className="p-8 text-center text-gray-500">No appointments found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="text-xs text-gray-500 uppercase bg-gray-50/50">
                <tr>
                  <th className="px-4 py-3 font-medium">Patient</th>
                  <th className="px-4 py-3 font-medium">Doctor</th>
                  <th className="px-4 py-3 font-medium">Department</th>
                  <th className="px-4 py-3 font-medium">Date</th>
                  <th className="px-4 py-3 font-medium">Time</th>
                  <th className="px-4 py-3 font-medium">Status</th>
                  <th className="px-4 py-3 font-medium text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {(appointments || []).map((appt) => (
                  <tr key={appt.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-4 py-4">
                      <span className="font-medium text-gray-900">{appt.patientName}</span>
                      <span className="text-gray-400 text-xs ml-1">({appt.patientId})</span>
                    </td>
                    <td className="px-4 py-4 text-gray-700">{appt.doctorName}</td>
                    <td className="px-4 py-4 text-gray-500">{appt.department}</td>
                    <td className="px-4 py-4 text-gray-500">{appt.appointmentDate}</td>
                    <td className="px-4 py-4 text-gray-500">{appt.appointmentTime}</td>
                    <td className="px-4 py-4">{badge(appt.status)}</td>
                    <td className="px-4 py-4 text-right space-x-3">
                      {appt.status === 'SCHEDULED' && (
                        <>
                          <button
                            onClick={() => handleStatus(appt, 'COMPLETED')}
                            className="text-success-600 hover:text-success-900 font-medium text-xs"
                          >
                            Complete
                          </button>
                          <button
                            onClick={() => handleStatus(appt, 'CANCELLED')}
                            className="text-danger-600 hover:text-danger-900 font-medium text-xs"
                          >
                            Cancel
                          </button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4" role="dialog" aria-modal="true" aria-label="Book appointment">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden">
            <div className="flex justify-between items-center p-6 border-b border-gray-100">
              <h2 className="text-lg font-bold text-gray-900">New Appointment</h2>
              <button onClick={() => setShowModal(false)} className="text-gray-400 hover:text-gray-600" aria-label="Close">
                <X className="h-5 w-5" />
              </button>
            </div>
            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              {error && (
                <div className="rounded-md bg-danger-50 border border-danger-500/30 px-4 py-3 text-sm text-danger-700">
                  {error}
                </div>
              )}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Patient ID (e.g. P001)</label>
                <input
                  required
                  type="text"
                  className="input-field"
                  placeholder="P001"
                  value={form.patientId}
                  onChange={(e) => setForm({ ...form, patientId: e.target.value })}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Doctor</label>
                <select
                  required
                  className="input-field"
                  value={form.doctorName}
                  onChange={(e) => setForm({ ...form, doctorName: e.target.value })}
                >
                  <option value="">Select a doctor</option>
                  {(doctors || []).map((doctor) => (
                    <option key={doctor.name} value={doctor.name}>
                      {doctor.name} ({doctor.department})
                    </option>
                  ))}
                </select>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Department</label>
                  <input
                    required
                    type="text"
                    className="input-field"
                    value={form.department}
                    onChange={(e) => setForm({ ...form, department: e.target.value })}
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Date</label>
                  <input
                    required
                    type="date"
                    className="input-field"
                    value={form.appointmentDate}
                    onChange={(e) => setForm({ ...form, appointmentDate: e.target.value })}
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Time (HH:MM)</label>
                <input
                  required
                  type="time"
                  className="input-field"
                  value={form.appointmentTime}
                  onChange={(e) => setForm({ ...form, appointmentTime: e.target.value })}
                />
              </div>
              <div className="pt-4 flex justify-end space-x-3">
                <button type="button" onClick={() => setShowModal(false)} className="btn btn-secondary">Cancel</button>
                <button type="submit" className="btn btn-primary">Book Appointment</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
