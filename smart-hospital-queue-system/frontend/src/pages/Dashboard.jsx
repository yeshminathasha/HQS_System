import React from 'react';
import { Link } from 'react-router-dom';
import { Card, CardHeader } from '../components/ui/Card';
import { Users, Clock, Activity, CalendarCheck, RefreshCw } from 'lucide-react';
import { patientService, reportService, doctorService } from '../services/api';
import { usePolling } from '../hooks/usePolling';
import { Badge } from '../components/ui/Badge';
import DoctorAvailability from '../components/DoctorAvailability';

export function Dashboard() {
  const { data: patients, loading, lastUpdated, refresh } = usePolling(
    () => patientService.getQueue().then((r) => r.data),
    10000
  );
  const { data: report } = usePolling(
    () => reportService.getDaily().then((r) => r.data),
    30000
  );
  const { data: doctors } = usePolling(
    () => doctorService.getDoctors().then((r) => r.data),
    30000
  );
  const { data: recommended } = usePolling(
    () => doctorService.recommend().then((r) => r.data),
    30000
  );

  const queue = patients || [];
  const totalInQueue = queue.length;
  const emergencyCases = queue.filter((p) => p.emergency).length;

  const workloadMap = {};
  queue.forEach((p) => {
    if (p.doctorName) {
      workloadMap[p.doctorName] = (workloadMap[p.doctorName] || 0) + 1;
    }
  });
  const workload = Object.entries(workloadMap).map(([doctorName, count]) => ({ doctorName, count }));

  const stats = [
    { name: 'Total in Queue', value: totalInQueue.toString(), icon: Users, color: 'text-primary-600', bg: 'bg-primary-50' },
    {
      name: 'Avg Wait Time (completed today)',
      value: report?.avgWaitMinutes != null ? report.avgWaitMinutes + 'm' : '—',
      icon: Clock,
      color: 'text-warning-600',
      bg: 'bg-warning-50',
    },
    { name: 'Emergency Cases', value: emergencyCases.toString(), icon: Activity, color: 'text-danger-600', bg: 'bg-danger-50' },
    { name: 'Completed Today', value: (report?.completed ?? 0).toString(), icon: CalendarCheck, color: 'text-success-600', bg: 'bg-success-50' },
  ];

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Hospital Dashboard</h1>
          <p className="text-gray-500 text-sm mt-1">
            Live overview of the hospital queue and patient flow.
          </p>
        </div>
        <div className="flex items-center space-x-3">
          <span className="text-xs text-gray-400">
            {lastUpdated ? `Updated ${lastUpdated.toLocaleTimeString()}` : 'Connecting...'}
          </span>
          <button
            onClick={refresh}
            className="inline-flex items-center text-xs font-medium text-primary-600 hover:text-primary-900"
          >
            <RefreshCw className="h-3.5 w-3.5 mr-1" /> Refresh
          </button>
        </div>
      </div>

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

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <Card className="h-full">
            <CardHeader title="Next in Queue" subtitle="Patients waiting, emergencies first" />
            <div className="overflow-x-auto">
              <table className="w-full text-sm text-left">
                <thead className="text-xs text-gray-500 uppercase bg-gray-50/50">
                  <tr>
                    <th className="px-4 py-3 font-medium">ID</th>
                    <th className="px-4 py-3 font-medium">Patient Name</th>
                    <th className="px-4 py-3 font-medium">Department</th>
                    <th className="px-4 py-3 font-medium">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {loading ? (
                    <tr><td colSpan="4" className="text-center p-4 text-gray-500">Loading data...</td></tr>
                  ) : queue.slice(0, 6).map((p) => (
                    <tr key={p.patientId || p.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-4 py-3 font-medium text-gray-900">{p.patientId}</td>
                      <td className="px-4 py-3 text-gray-700">{p.name}</td>
                      <td className="px-4 py-3 text-gray-500">{p.department}</td>
                      <td className="px-4 py-3">
                        {p.emergency ? (
                          <Badge variant="danger">Emergency</Badge>
                        ) : p.status === 'WAITING' ? (
                          <Badge variant="warning">Waiting</Badge>
                        ) : (
                          <Badge variant="primary">In Consultation</Badge>
                        )}
                      </td>
                    </tr>
                  ))}
                  {!loading && queue.length === 0 && (
                    <tr><td colSpan="4" className="text-center p-4 text-gray-500">No patients in queue</td></tr>
                  )}
                </tbody>
              </table>
            </div>
            <div className="mt-4">
              <Link to="/queue" className="text-sm font-medium text-primary-600 hover:text-primary-900">
                Manage full queue →
              </Link>
            </div>
          </Card>
        </div>

        <div className="lg:col-span-1">
          <Card className="h-full">
            <CardHeader title="Doctor Availability" subtitle="Current queue lengths per doctor" />
            <DoctorAvailability doctors={doctors || []} workload={workload} loading={loading} recommended={recommended} />
          </Card>
        </div>
      </div>
    </div>
  );
}
