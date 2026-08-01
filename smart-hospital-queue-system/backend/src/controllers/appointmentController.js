const Appointment = require('../models/Appointment');

const appointments = [];

exports.getAppointments = (req, res) => {
  res.json(appointments);
};

exports.createAppointment = (req, res) => {
  const { id, patientId, doctorId, time } = req.body;
  const appointment = new Appointment(id, patientId, doctorId, time);
  appointments.push(appointment);
  res.status(201).json(appointment);
};
