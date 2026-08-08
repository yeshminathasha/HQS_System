import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { DashboardLayout } from './components/layout/DashboardLayout';
import { Dashboard } from './pages/Dashboard';
import { PatientQueue } from './pages/PatientQueue';
import Emergency from './pages/Emergency';
import Appointments from './pages/Appointments';
import History from './pages/History';
import Register from './pages/Register';
import Reports from './pages/Reports';

function App() {
  return (
    <Router>
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
    </Router>
  );

  <Route path="/app" element={<UserLayout />}>
  <Route index element={<UserDashboardPage />} />
  <Route path="book" element={<BookAppointmentPage />} />
  <Route path="my-appointments" element={<MyAppointmentsPage />} />
  <Route path="queue" element={<LiveQueuePage />} />
  <Route path="history" element={<MyHistoryPage />} />
  <Route path="reports" element={<ReportsReadOnlyPage />} />
  <Route path="profile" element={<ProfilePage />} />
</Route>

}

export default App;
