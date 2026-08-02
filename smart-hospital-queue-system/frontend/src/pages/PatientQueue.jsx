import React, { useEffect, useState } from 'react';
import { Card } from '../components/ui/Card';
import { Plus, Search, X } from 'lucide-react';
import { patientService, doctorService } from '../services/api';
import { usePolling } from '../hooks/usePolling';
import PatientForm from '../components/PatientForm';
import QueueDisplay from '../components/QueueDisplay';

export function PatientQueue() {
  const [showModal, setShowModal] = useState(false);
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [departmentFilter, setDepartmentFilter] = useState('');
  const [emergencyFilter, setEmergencyFilter] = useState('');

  useEffect(() => {
    const id = setTimeout(() => setDebouncedSearch(search), 400);
    return () => clearTimeout(id);
  }, [search]);

  const { data: patients, loading, error, lastUpdated, refresh } = usePolling(
    () =>
      patientService
        .getQueue({
          search: debouncedSearch || undefined,
          status: statusFilter || undefined,
          department: departmentFilter || undefined,
          emergency: emergencyFilter === 'emergency' ? true : emergencyFilter === 'normal' ? false : undefined,
        })
        .then((r) => r.data),
    10000,
    [debouncedSearch, statusFilter, departmentFilter, emergencyFilter]
  );

  const { data: doctors } = usePolling(() => doctorService.getDoctors().then((r) => r.data), 60000);

  const handleRegister = async (formData) => {
    await patientService.registerPatient(formData);
    setShowModal(false);
    refresh();
  };

  const handleCancel = async (patient) => {
    if (window.confirm(`Cancel appointment for ${patient.name} (${patient.patientId})?`)) {
      try {
        await patientService.cancelPatient(patient.patientId);
        refresh();
      } catch (err) {
        alert(err.displayMessage || 'Failed to cancel appointment');
      }
    }
  };

  const handleCallNext = async (patient) => {
    try {
      await patientService.updateStatus(patient.patientId, 'IN_CONSULTATION');
      refresh();
    } catch (err) {
      alert(err.displayMessage || 'Failed to update status');
    }
  };

  const handleComplete = async (patient) => {
    try {
      await patientService.updateStatus(patient.patientId, 'COMPLETED');
      refresh();
    } catch (err) {
      alert(err.displayMessage || 'Failed to update status');
    }
  };

  return (
    <div className="space-y-6 relative">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Patient Queue</h1>
          <p className="text-gray-500 text-sm mt-1">
            Manage the active queue. Emergencies are served first by priority.
          </p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          <Plus className="h-4 w-4 mr-2" />
          Register Patient
        </button>
      </div>

      <Card>
        <div className="flex flex-col lg:flex-row justify-between items-center gap-4 mb-6 border-b border-gray-100 pb-4">
          <div className="relative w-full lg:w-96">
            <span className="absolute inset-y-0 left-0 flex items-center pl-3">
              <Search className="h-4 w-4 text-gray-400" />
            </span>
            <input
              type="text"
              className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg leading-5 bg-gray-50 placeholder-gray-500 focus:outline-none focus:bg-white focus:ring-1 focus:ring-primary-500 focus:border-primary-500 sm:text-sm transition-colors"
              placeholder="Search by ID, name, doctor or department..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <div className="flex items-center gap-3">
            <select
              className="input-field w-auto"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="WAITING">Waiting</option>
              <option value="IN_CONSULTATION">In Consultation</option>
            </select>
            <select
              className="input-field w-auto"
              value={departmentFilter}
              onChange={(e) => setDepartmentFilter(e.target.value)}
            >
              <option value="">All Departments</option>
              <option>General</option>
              <option>Cardiology</option>
              <option>Neurology</option>
              <option>Orthopedics</option>
            </select>
            <select
              className="input-field w-auto"
              value={emergencyFilter}
              onChange={(e) => setEmergencyFilter(e.target.value)}
            >
              <option value="">All Cases</option>
              <option value="emergency">Emergency Only</option>
              <option value="normal">Normal Only</option>
            </select>
          </div>
        </div>

        <QueueDisplay
          patients={patients || []}
          loading={loading}
          error={error}
          lastUpdated={lastUpdated}
          onRefresh={refresh}
          onCallNext={handleCallNext}
          onComplete={handleComplete}
          onCancel={handleCancel}
        />
      </Card>

      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4" role="dialog" aria-modal="true" aria-label="Register new patient">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden">
            <div className="flex justify-between items-center p-6 border-b border-gray-100">
              <h2 className="text-lg font-bold text-gray-900">Register New Patient</h2>
              <button onClick={() => setShowModal(false)} className="text-gray-400 hover:text-gray-600" aria-label="Close">
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="p-6">
              <PatientForm
                doctors={doctors || []}
                onSubmit={handleRegister}
                onCancel={() => setShowModal(false)}
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
