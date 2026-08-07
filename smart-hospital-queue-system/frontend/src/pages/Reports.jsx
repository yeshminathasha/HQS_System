import React, { useState } from 'react';
import { Card } from '../components/ui/Card';
import { CalendarDays } from 'lucide-react';
import { reportService } from '../services/api';
import { usePolling } from '../hooks/usePolling';
import ReportsView from '../components/ReportsView';

export default function Reports() {
  const now = new Date();
  const today = new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
  const [date, setDate] = useState(today);

  const { data: report, loading, error } = usePolling(
    () => reportService.getDaily(date).then((r) => r.data),
    30000,
    [date]
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Daily Report</h1>
          <p className="text-gray-500 text-sm mt-1">
            Registrations, completions, cancellations and average wait times.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <CalendarDays className="h-4 w-4 text-gray-400" />
          <input
            type="date"
            className="input-field w-auto"
            value={date}
            max={today}
            onChange={(e) => setDate(e.target.value || today)}
          />
        </div>
      </div>

      <Card className="p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-1">
          Report for {report?.date || date}
        </h2>
        <p className="text-sm text-gray-500 mb-6">Aggregated from live patient records.</p>
        <ReportsView report={report} loading={loading} error={error} />
      </Card>
    </div>
  );
}
