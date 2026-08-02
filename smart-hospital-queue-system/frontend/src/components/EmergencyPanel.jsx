import React from 'react';
import { Badge } from './ui/Badge';
import { Siren } from 'lucide-react';
import { formatTime } from './QueueDisplay';

const PRIORITY_STYLES = {
  1: { label: 'Priority 1 - Critical', variant: 'danger' },
  2: { label: 'Priority 2 - Urgent', variant: 'warning' },
  3: { label: 'Priority 3 - Standard', variant: 'primary' },
};

export default function EmergencyPanel({ patients = [], loading = false, error = null, onCallNext }) {
  const sorted = [...patients].sort((a, b) => a.priorityLevel - b.priorityLevel);

  return (
    <div className="space-y-4">
      {error && (
        <div className="rounded-md bg-danger-50 border border-danger-500/30 px-4 py-3 text-sm text-danger-700">
          {error.displayMessage || 'Failed to load emergency cases.'}
        </div>
      )}
      {loading ? (
        <div className="p-8 text-center text-gray-500">Loading emergency cases...</div>
      ) : sorted.length === 0 ? (
        <div className="p-8 text-center text-gray-500">No emergency cases right now.</div>
      ) : (
        sorted.map((patient) => {
          const style = PRIORITY_STYLES[patient.priorityLevel] || PRIORITY_STYLES[3];
          return (
            <div
              key={patient.patientId || patient.id}
              className="flex items-center justify-between rounded-lg border border-danger-200 bg-danger-50/50 p-4"
            >
              <div className="flex items-start space-x-3">
                <div className="p-2 rounded-lg bg-danger-600 text-white">
                  <Siren className="h-4 w-4" />
                </div>
                <div>
                  <div className="flex items-center space-x-2">
                    <span className="font-semibold text-gray-900">{patient.name}</span>
                    <Badge variant={style.variant}>{style.label}</Badge>
                  </div>
                  <p className="text-sm text-gray-500 mt-0.5">
                    {patient.patientId} · {patient.department} · {patient.doctorName}
                  </p>
                  <p className="text-xs text-gray-400 mt-0.5">Registered {formatTime(patient.registeredAt)}</p>
                </div>
              </div>
              {onCallNext && patient.status === 'WAITING' && (
                <button
                  onClick={() => onCallNext(patient)}
                  className="btn btn-danger text-xs px-3 py-1.5"
                >
                  Call Next
                </button>
              )}
            </div>
          );
        })
      )}
    </div>
  );
}
