package com.smarthospital.repository;

import com.smarthospital.entity.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {

    Optional<Appointment> findByPatientIdAndAppointmentDateAndAppointmentTime(
            String patientId, LocalDate date, String time);

    Optional<Appointment> findByDoctorNameAndAppointmentDateAndAppointmentTime(
            String doctorName, LocalDate date, String time);

    List<Appointment> findByBookedByUserId(String bookedByUserId);
}