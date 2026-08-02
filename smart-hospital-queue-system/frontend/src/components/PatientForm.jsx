import React, { useState } from 'react';

const DEPARTMENTS = ['General', 'Cardiology', 'Neurology', 'Orthopedics'];

export default function PatientForm({ initialValues = {}, doctors = [], onSubmit, submitLabel = 'Register Patient', onCancel }) {
  const [form, setForm] = useState({
    name: '',
    contactNumber: '',
    department: 'General',
    doctorName: '',
    emergency: false,
    priorityLevel: 1,
    ...initialValues,
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const set = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await onSubmit(form);
    } catch (err) {
      setError(err.displayMessage || 'Failed to submit. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {error && (
        <div className="rounded-md bg-danger-50 border border-danger-500/30 px-4 py-3 text-sm text-danger-700">
          {error}
        </div>
      )}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
        <input
          required
          type="text"
          className="input-field"
          value={form.name}
          onChange={(e) => set('name', e.target.value)}
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Contact Number</label>
        <input
          required
          type="text"
          pattern="[0-9+\-() ]{7,20}"
          title="Enter a valid phone number (7-20 digits)"
          className="input-field"
          value={form.contactNumber}
          onChange={(e) => set('contactNumber', e.target.value)}
        />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Department</label>
          <select
            className="input-field"
            value={form.department}
            onChange={(e) => set('department', e.target.value)}
          >
            {DEPARTMENTS.map((d) => (
              <option key={d}>{d}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Assigned Doctor</label>
          <input
            required
            type="text"
            list="doctor-options"
            className="input-field"
            value={form.doctorName}
            onChange={(e) => set('doctorName', e.target.value)}
          />
          <datalist id="doctor-options">
            {doctors.map((doctor) => (
              <option key={doctor.name} value={doctor.name}>
                {doctor.name} ({doctor.department})
              </option>
            ))}
          </datalist>
        </div>
      </div>
      <div className="flex items-center mt-4">
        <input
          type="checkbox"
          id="emergency"
          className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
          checked={form.emergency}
          onChange={(e) => set('emergency', e.target.checked)}
        />
        <label htmlFor="emergency" className="ml-2 block text-sm text-danger-600 font-medium">
          Emergency Case
        </label>
      </div>
      {form.emergency && (
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Priority Level (1 - Highest, 3 - Lowest)
          </label>
          <select
            className="input-field"
            value={form.priorityLevel}
            onChange={(e) => set('priorityLevel', parseInt(e.target.value, 10))}
          >
            <option value={1}>1 - High</option>
            <option value={2}>2 - Medium</option>
            <option value={3}>3 - Low</option>
          </select>
        </div>
      )}
      <div className="pt-4 flex justify-end space-x-3">
        {onCancel && (
          <button type="button" onClick={onCancel} className="btn btn-secondary">
            Cancel
          </button>
        )}
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Saving...' : submitLabel}
        </button>
      </div>
    </form>
  );
}
