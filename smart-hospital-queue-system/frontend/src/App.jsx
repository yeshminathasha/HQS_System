import React, { lazy, Suspense } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { DashboardLayout } from './components/layout/DashboardLayout';

const Dashboard = lazy(() => import('./pages/Dashboard').then((m) => ({ default: m.Dashboard })));
const PatientQueue = lazy(() => import('./pages/PatientQueue').then((m) => ({ default: m.PatientQueue })));
const Emergency = lazy(() => import('./pages/Emergency'));
const Appointments = lazy(() => import('./pages/Appointments'));
const History = lazy(() => import('./pages/History'));
const Register = lazy(() => import('./pages/Register'));
const Reports = lazy(() => import('./pages/Reports'));

function PageFallback() {
  return <div className="p-12 text-center text-gray-500">Loading...</div>;
}

function App() {
  return (
    <Router>
      <Suspense fallback={<PageFallback />}>
        <Routes>
          <Route path="/" element={<DashboardLayout />}>
            <Route index element={<Dashboard />} />
            <Route path="queue" element={<PatientQueue />} />
            <Route path="emergency" element={<Emergency />} />
            <Route path="appointments" element={<Appointments />} />
            <Route path="history" element={<History />} />
            <Route path="register" element={<Register />} />
            <Route path="reports" element={<Reports />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </Suspense>
    </Router>
  );
}

export default App;
