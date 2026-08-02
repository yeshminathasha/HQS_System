import React, { useState } from 'react';
import { Card, CardHeader } from '../components/ui/Card';
import { UserPlus, CheckCircle2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import { patientService, doctorService } from '../services/api';
import { usePolling } from '../hooks/usePolling';
import PatientForm from '../components/PatientForm';

export default function Register() {
  const [success, setSuccess] = useState(null);
  const { data: doctors } = usePolling(() => doctorService.getDoctors().then((r) => r.data), 60000);

  const handleSubmit = async (formData) => {
    const response = await patientService.registerPatient(formData);
    setSuccess(response.data);
  };

  return (
    <div className="max-w-xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Register Patient</h1>
        <p className="text-gray-500 text-sm mt-1">
          Register a new patient into the queue. Emergency cases are prioritized automatically.
        </p>
      </div>

      {success ? (
        <Card>
          <div className="text-center py-6">
            <CheckCircle2 className="h-12 w-12 text-success-500 mx-auto mb-3" />
            <h2 className="text-lg font-bold text-gray-900">Patient registered successfully</h2>
            <p className="text-gray-500 mt-1">
              {success.name} has been assigned ID <span className="font-semibold text-gray-900">{success.patientId}</span>
            </p>
            <div className="mt-6 flex justify-center space-x-3">
              <Link to="/queue" className="btn btn-primary">View Queue</Link>
              <button className="btn btn-secondary" onClick={() => setSuccess(null)}>
                Register Another
              </button>
            </div>
          </div>
        </Card>
      ) : (
        <Card>
          <CardHeader title="New Patient" subtitle="All fields are required" />
          <PatientForm doctors={doctors || []} onSubmit={handleSubmit} submitLabel="Register Patient" />
        </Card>
      )}
    </div>
  );
}
