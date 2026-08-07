package com.smarthospital.repository;

import com.smarthospital.entity.Appointment;
import com.smarthospital.entity.AppointmentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    Optional<Appointment> findByPatientIdAndAppointmentDateAndAppointmentTimeAndStatus(
            String patientId, LocalDate date, String time, AppointmentStatus status);
    Optional<Appointment> findByDoctorNameAndAppointmentDateAndAppointmentTimeAndStatus(
            String doctorName, LocalDate date, String time, AppointmentStatus status);
}
