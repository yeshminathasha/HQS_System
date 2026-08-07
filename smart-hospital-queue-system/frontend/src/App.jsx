import React, { Suspense, lazy } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./auth/AuthContext";
import ProtectedRoute from "./auth/ProtectedRoute";
import RoleRoute from "./auth/RoleRoute";

import LoginPage from "./pages/auth/LoginPage";
import RegisterPage from "./pages/auth/RegisterPage";

import AdminLayout from "./components/layout/AdminLayout";
import AdminDashboardPage from "./pages/admin/AdminDashboardPage";

import UserLayout from "./components/layout/UserLayout";
import UserDashboardPage from "./pages/user/UserDashboardPage";

// Preserve existing routes for Member B reference
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

export default function App() {
  return (
    <Router>
      <AuthProvider>
        <Suspense fallback={<PageFallback />}>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            <Route element={<ProtectedRoute />}>
              <Route element={<RoleRoute role="ADMIN" />}>
                <Route path="/admin" element={<AdminLayout />}>
                  <Route index element={<AdminDashboardPage />} />
                  {/* Future admin routes will go here */}
                </Route>
              </Route>

              <Route element={<RoleRoute role="USER" />}>
                <Route path="/app" element={<UserLayout />}>
                  <Route index element={<UserDashboardPage />} />
                  {/* Preserved existing routes from before the Auth rewrite */}
                  <Route path="legacy" element={<DashboardLayout />}>
                    <Route index element={<Dashboard />} />
                    <Route path="queue" element={<PatientQueue />} />
                    <Route path="emergency" element={<Emergency />} />
                    <Route path="appointments" element={<Appointments />} />
                    <Route path="history" element={<History />} />
                    <Route path="register" element={<Register />} />
                    <Route path="reports" element={<Reports />} />
                  </Route>
                </Route>
              </Route>
            </Route>

            <Route path="/unauthorized" element={<div className="p-12 text-center text-xl text-red-600">Not authorized</div>} />
            <Route path="*" element={<Navigate to="/login" />} />
          </Routes>
        </Suspense>
      </AuthProvider>
    </Router>
  );
}
