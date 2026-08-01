const Patient = require('../models/Patient');

const patients = [];

exports.getPatients = (req, res) => {
  res.json(patients);
};

exports.createPatient = (req, res) => {
  const { id, name, priority } = req.body;
  const patient = new Patient(id, name, priority);
  patients.push(patient);
  res.status(201).json(patient);
};
