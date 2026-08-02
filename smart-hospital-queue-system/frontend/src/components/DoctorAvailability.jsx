import React from 'react';
import { User, CalendarClock } from 'lucide-react';

export default function DoctorAvailability({ doctors = [], workload = [], loading = false, recommended = null }) {
  const countFor = (doctorName) => {
    const entry = workload.find((w) => w.doctorName === doctorName);
    return entry ? entry.count : 0;
  };
  const maxCount = Math.max(1, ...workload.map((w) => w.count));
  const doctorsToShow = doctors.length > 0 ? doctors : [];

  if (loading) {
    return <div className="p-8 text-center text-gray-500">Loading doctor availability...</div>;
  }

  if (doctorsToShow.length === 0) {
    return <div className="text-sm text-gray-500">No doctors registered.</div>;
  }

  return (
    <div className="space-y-4">
      {recommended && (
        <div className="rounded-md bg-success-500/10 border border-success-500/30 px-4 py-3 text-sm">
          <span className="font-medium text-success-500">Recommended: </span>
          <span className="text-gray-700">
            {recommended.doctorName} ({recommended.department}) — {recommended.queueCount} waiting
          </span>
        </div>
      )}
      {doctorsToShow.map((doctor) => {
        const count = countFor(doctor.name);
        const width = Math.round((count / maxCount) * 100);
        return (
          <div key={doctor.name} className="flex items-center justify-between">
            <div className="flex items-center min-w-0 flex-1">
              <div className="h-8 w-8 rounded-full bg-primary-100 flex items-center justify-center text-primary-700 mr-3">
                <User className="h-4 w-4" />
              </div>
              <div className="min-w-0">
                <p className="text-sm font-medium text-gray-700 truncate">{doctor.name}</p>
                <p className="text-xs text-gray-400">{doctor.department}</p>
              </div>
            </div>
            <div className="flex items-center space-x-3 ml-4">
              <div className="w-24 h-2 rounded-full bg-gray-100 overflow-hidden">
                <div
                  className={`h-full rounded-full ${count > 0 ? 'bg-warning-500' : 'bg-success-500'}`}
                  style={{ width: `${width}%` }}
                />
              </div>
              <span className="text-sm text-gray-500 w-16 text-right">{count} waiting</span>
            </div>
          </div>
        );
      })}
      <div className="pt-2 flex items-center text-xs text-gray-400">
        <CalendarClock className="h-3.5 w-3.5 mr-1.5" />
        Schedule: Mon-Fri 09:00-17:00 (varies by doctor)
      </div>
    </div>
  );
}
