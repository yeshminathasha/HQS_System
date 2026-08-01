class Appointment {
  constructor(id, patientId, doctorId, time) {
    this.id = id;
    this.patientId = patientId;
    this.doctorId = doctorId;
    this.time = time;
  }
}

module.exports = Appointment;
