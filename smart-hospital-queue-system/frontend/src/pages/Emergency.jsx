import React from 'react';
import { Card, CardHeader } from '../components/ui/Card';
import { patientService } from '../services/api';
import { usePolling } from '../hooks/usePolling';
import EmergencyPanel from '../components/EmergencyPanel';

export default function Emergency() {
  const { data: patients, loading, error, lastUpdated, refresh } = usePolling(
    () => patientService.getQueue({ emergency: true }).then((r) => r.data),
    10000
  );

  const handleCallNext = async (patient) => {
    try {
      await patientService.updateStatus(patient.patientId, 'IN_CONSULTATION');
      refresh();
    } catch (err) {
      alert(err.displayMessage || 'Failed to update status');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Emergency Cases</h1>
          <p className="text-gray-500 text-sm mt-1">
            Emergency patients sorted by priority — always served first.
          </p>
        </div>
        <span className="text-xs text-gray-400">
          {lastUpdated ? `Updated ${lastUpdated.toLocaleTimeString()}` : 'Connecting...'}
        </span>
      </div>
      <Card>
        <CardHeader title="Active Emergencies" subtitle="Prioritized by severity (1 = critical)" />
        <EmergencyPanel
          patients={patients || []}
          loading={loading}
          error={error}
          onCallNext={handleCallNext}
        />
      </Card>
    </div>
  );
}
