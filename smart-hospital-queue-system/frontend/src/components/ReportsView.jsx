import React from 'react';
import { Card } from './ui/Card';
import { Users, ClipboardCheck, XCircle, Timer, UserCog, Building2 } from 'lucide-react';

export default function ReportsView({ report = null, loading = false, error = null }) {
  if (loading) {
    return <div className="p-8 text-center text-gray-500">Loading daily report...</div>;
  }

  if (error) {
    return (
      <div className="rounded-md bg-danger-50 border border-danger-500/30 px-4 py-3 text-sm text-danger-700">
        {error.displayMessage || 'Failed to load the report.'}
      </div>
    );
  }

  if (!report) {
    return <div className="p-8 text-center text-gray-500">No report data.</div>;
  }

  const stats = [
    { name: 'Registered Today', value: report.totalRegistered, icon: Users, color: 'text-primary-600', bg: 'bg-primary-50' },
    { name: 'Completed', value: report.completed, icon: ClipboardCheck, color: 'text-success-600', bg: 'bg-success-50' },
    { name: 'Cancelled', value: report.cancelled, icon: XCircle, color: 'text-danger-600', bg: 'bg-danger-50' },
    { name: 'Avg Wait Time', value: report.avgWaitMinutes != null ? `${report.avgWaitMinutes}m` : '—', icon: Timer, color: 'text-warning-600', bg: 'bg-warning-50' },
  ];

  const statTable = (title, icon, rows) => (
    <Card className="h-full">
      <div className="flex items-center mb-4">
        {icon}
        <h3 className="text-lg font-semibold text-gray-900 ml-2">{title}</h3>
      </div>
      {rows.length === 0 ? (
        <p className="text-sm text-gray-500">No data for this period.</p>
      ) : (
        <table className="w-full text-sm text-left">
          <tbody className="divide-y divide-gray-100">
            {rows.map((row) => (
              <tr key={row.name}>
                <td className="px-2 py-2.5 text-gray-700">{row.name}</td>
                <td className="px-2 py-2.5 text-right font-medium text-gray-900">{row.count}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </Card>
  );

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat) => (
          <Card key={stat.name} className="flex items-center p-6">
            <div className={`p-4 rounded-xl ${stat.bg} mr-4`}>
              <stat.icon className={`h-6 w-6 ${stat.color}`} />
            </div>
            <div>
              <p className="text-sm font-medium text-gray-500">{stat.name}</p>
              <p className="text-2xl font-bold text-gray-900 mt-1">{stat.value}</p>
            </div>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {statTable('By Department', <Building2 className="h-5 w-5 text-primary-600" />, report.byDepartment || [])}
        {statTable('By Doctor', <UserCog className="h-5 w-5 text-primary-600" />, report.byDoctor || [])}
      </div>
    </div>
  );
}
