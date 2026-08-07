import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.message ||
      (error.response ? `Request failed (${error.response.status})` : 'Cannot reach the server');
    error.displayMessage = message;
    return Promise.reject(error);
  }
);

export const patientService = {
  getQueue: (params) => api.get('/patients/queue', { params }),
  getHistory: (params) => api.get('/patients/history', { params }),
  getPatientHistory: (id, params) => api.get(`/patients/${id}/history`, { params }),
  getPatient: (id) => api.get(`/patients/${id}`),
  registerPatient: (data) => api.post('/patients', data),
  updatePatient: (id, data) => api.put(`/patients/${id}`, data),
  updateStatus: (id, status) => api.patch(`/patients/${id}/status`, { status }),
  getWaitTime: (id) => api.get(`/patients/${id}/wait-time`),
  cancelPatient: (id) => api.patch(`/patients/${id}/status`, { status: 'CANCELLED' }),
};

export const appointmentService = {
  getAppointments: (params) => api.get('/appointments', { params }),
  createAppointment: (data) => api.post('/appointments', data),
  updateAppointment: (id, data) => api.put(`/appointments/${id}`, data),
  updateStatus: (id, status) => api.patch(`/appointments/${id}/status`, { status }),
};

export const doctorService = {
  getDoctors: () => api.get('/doctors'),
  recommend: () => api.get('/doctors/recommend'),
};

export const reportService = {
  getDaily: (date) => api.get('/reports/daily', { params: { date } }),
};

export default api;
