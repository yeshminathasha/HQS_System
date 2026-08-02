package com.smarthospital.repository;

import com.smarthospital.entity.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    Optional<Appointment> findByPatientIdAndAppointmentDateAndAppointmentTime(String patientId, LocalDate date, String time);
    Optional<Appointment> findByDoctorNameAndAppointmentDateAndAppointmentTime(String doctorName, LocalDate date, String time);
}
