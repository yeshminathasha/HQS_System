import React from 'react';
import { Badge } from './ui/Badge';
import { RefreshCw, AlertCircle } from 'lucide-react';

export const STATUS_STYLES = {
  WAITING: { label: 'Waiting', variant: 'warning' },
  IN_CONSULTATION: { label: 'In Consultation', variant: 'primary' },
  COMPLETED: { label: 'Completed', variant: 'success' },
  CANCELLED: { label: 'Cancelled', variant: 'neutral' },
};

export function formatTime(timestamp) {
  if (!timestamp) return '—';
  const date = new Date(timestamp);
  return date.toLocaleString(undefined, {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function QueueDisplay({
  patients = [],
  loading = false,
  error = null,
  lastUpdated = null,
  onCallNext,
  onComplete,
  onCancel,
  showActions = true,
  emptyMessage = 'No patients in the queue.',
  onRefresh,
}) {
  const statusBadge = (patient) => {
    if (patient.emergency) {
      return <Badge variant="danger">Emergency (Lvl {patient.priorityLevel})</Badge>;
    }
    const style = STATUS_STYLES[patient.status] || STATUS_STYLES.WAITING;
    return <Badge variant={style.variant}>{style.label}</Badge>;
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <p className="text-xs text-gray-400">
          {lastUpdated ? `Last updated ${lastUpdated.toLocaleTimeString()}` : 'Waiting for data...'}
        </p>
        {onRefresh && (
          <button
            onClick={onRefresh}
            className="inline-flex items-center text-xs font-medium text-primary-600 hover:text-primary-900"
          >
            <RefreshCw className="h-3.5 w-3.5 mr-1" /> Refresh
          </button>
        )}
      </div>

      {error && (
        <div className="flex items-center gap-2 rounded-md bg-danger-50 border border-danger-500/30 px-4 py-3 text-sm text-danger-700 mb-4">
          <AlertCircle className="h-4 w-4" />
          <span>{error.displayMessage || 'Failed to load the queue.'}</span>
        </div>
      )}

      {loading ? (
        <div className="p-8 text-center text-gray-500">Loading live queue...</div>
      ) : patients.length === 0 ? (
        <div className="p-8 text-center text-gray-500">{emptyMessage}</div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-sm text-left">
            <thead className="text-xs text-gray-500 uppercase bg-gray-50/50">
              <tr>
                <th className="px-4 py-3 font-medium">Patient ID</th>
                <th className="px-4 py-3 font-medium">Name</th>
                <th className="px-4 py-3 font-medium">Department</th>
                <th className="px-4 py-3 font-medium">Assigned Doctor</th>
                <th className="px-4 py-3 font-medium">Registered</th>
                <th className="px-4 py-3 font-medium">Status</th>
                {showActions && <th className="px-4 py-3 font-medium text-right">Actions</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {patients.map((patient) => (
                <tr key={patient.patientId || patient.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-4 font-medium text-gray-900">{patient.patientId}</td>
                  <td className="px-4 py-4 text-gray-700">
                    <div className="flex items-center">
                      <div className="h-8 w-8 rounded-full bg-primary-100 flex items-center justify-center text-primary-700 font-bold mr-3">
                        {(patient.name || '?').charAt(0).toUpperCase()}
                      </div>
                      {patient.name}
                    </div>
                  </td>
                  <td className="px-4 py-4 text-gray-500">{patient.department}</td>
                  <td className="px-4 py-4 text-gray-500">{patient.doctorName}</td>
                  <td className="px-4 py-4 text-gray-400 text-xs">{formatTime(patient.registeredAt)}</td>
                  <td className="px-4 py-4">{statusBadge(patient)}</td>
                  {showActions && (
                    <td className="px-4 py-4 text-right space-x-3">
                      {patient.status === 'WAITING' && onCallNext && (
                        <button
                          onClick={() => onCallNext(patient)}
                          className="text-primary-600 hover:text-primary-900 font-medium text-xs"
                        >
                          Call Next
                        </button>
                      )}
                      {patient.status === 'IN_CONSULTATION' && onComplete && (
                        <button
                          onClick={() => onComplete(patient)}
                          className="text-success-600 hover:text-success-900 font-medium text-xs"
                        >
                          Complete
                        </button>
                      )}
                      {patient.status === 'WAITING' && onCancel && (
                        <button
                          onClick={() => onCancel(patient)}
                          className="text-danger-600 hover:text-danger-900 font-medium text-xs"
                        >
                          Cancel
                        </button>
                      )}
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
