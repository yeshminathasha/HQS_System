import React, { useEffect, useState } from 'react';
import { Card, CardHeader } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { Search } from 'lucide-react';
import { patientService } from '../services/api';
import { usePolling } from '../hooks/usePolling';
import { formatTime } from '../components/QueueDisplay';
import Pagination from '../components/ui/Pagination';

const PAGE_SIZE = 20;

export default function History() {
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [patientFilter, setPatientFilter] = useState('');
  const [debouncedPatient, setDebouncedPatient] = useState('');
  const [page, setPage] = useState(0);

  useEffect(() => {
    const id = setTimeout(() => {
      setDebouncedSearch(search);
      setPage(0);
    }, 400);
    return () => clearTimeout(id);
  }, [search]);

  useEffect(() => {
    const id = setTimeout(() => {
      setDebouncedPatient(patientFilter.trim());
      setPage(0);
    }, 400);
    return () => clearTimeout(id);
  }, [patientFilter]);

  const fetcher = () => {
    if (debouncedPatient) {
      return patientService
        .getPatientHistory(debouncedPatient, { page, size: PAGE_SIZE })
        .then((r) => r.data);
    }
    return patientService
      .getHistory({ search: debouncedSearch || undefined, page, size: PAGE_SIZE })
      .then((r) => r.data);
  };

  const { data: pageData, loading, error, lastUpdated } = usePolling(
    fetcher,
    15000,
    [debouncedSearch, debouncedPatient, page]
  );

  const patients = pageData?.content || [];
  const totalPages = pageData?.totalPages || 0;

  const badge = (status) => {
    if (status === 'COMPLETED') return <Badge variant="success">Completed</Badge>;
    if (status === 'CANCELLED') return <Badge variant="neutral">Cancelled</Badge>;
    return <Badge variant="primary">{status}</Badge>;
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Appointment History</h1>
        <p className="text-gray-500 text-sm mt-1">Completed and cancelled appointments.</p>
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
              placeholder="Search history by ID, name or doctor..."
              value={search}
              disabled={!!debouncedPatient}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <input
            type="text"
            className="input-field w-full lg:w-56"
            placeholder="Patient ID only (e.g. P001)"
            value={patientFilter}
            onChange={(e) => setPatientFilter(e.target.value)}
          />
          <span className="text-xs text-gray-400">
            {lastUpdated ? `Updated ${lastUpdated.toLocaleTimeString()}` : 'Connecting...'}
          </span>
        </div>

        {error && (
          <div className="rounded-md bg-danger-50 border border-danger-500/30 px-4 py-3 text-sm text-danger-700 mb-4">
            {error.displayMessage || 'Failed to load history.'}
          </div>
        )}

        {loading ? (
          <div className="p-8 text-center text-gray-500">Loading history...</div>
        ) : patients.length === 0 ? (
          <div className="p-8 text-center text-gray-500">No history records found.</div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-sm text-left">
                <thead className="text-xs text-gray-500 uppercase bg-gray-50/50">
                  <tr>
                    <th className="px-4 py-3 font-medium">Patient ID</th>
                    <th className="px-4 py-3 font-medium">Name</th>
                    <th className="px-4 py-3 font-medium">Department</th>
                    <th className="px-4 py-3 font-medium">Doctor</th>
                    <th className="px-4 py-3 font-medium">Registered</th>
                    <th className="px-4 py-3 font-medium">Wait Time</th>
                    <th className="px-4 py-3 font-medium">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {patients.map((patient) => (
                    <tr key={patient.patientId || patient.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-4 py-3 font-medium text-gray-900">{patient.patientId}</td>
                      <td className="px-4 py-3 text-gray-700">{patient.name}</td>
                      <td className="px-4 py-3 text-gray-500">{patient.department}</td>
                      <td className="px-4 py-3 text-gray-500">{patient.doctorName}</td>
                      <td className="px-4 py-3 text-gray-400 text-xs">{formatTime(patient.registeredAt)}</td>
                      <td className="px-4 py-3 text-gray-500">
                        {patient.status === 'COMPLETED' && patient.waitMinutes > 0 ? `${patient.waitMinutes}m` : '—'}
                      </td>
                      <td className="px-4 py-3">{badge(patient.status)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination
              page={page}
              totalPages={totalPages}
              totalElements={pageData?.totalElements}
              onPageChange={setPage}
            />
          </>
        )}
      </Card>
    </div>
  );
}
