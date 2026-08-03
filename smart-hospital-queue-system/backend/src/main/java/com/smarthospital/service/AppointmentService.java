package com.smarthospital.service;

import com.smarthospital.dto.AppointmentRequest;
import com.smarthospital.dto.AppointmentResponse;
import com.smarthospital.dto.PatientResponse;
import com.smarthospital.entity.Appointment;
import com.smarthospital.exception.AppointmentNotFoundException;
import com.smarthospital.exception.InvalidStatusTransitionException;
import com.smarthospital.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final MongoTemplate mongoTemplate;
    private final PatientService patientService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              MongoTemplate mongoTemplate,
                              PatientService patientService) {
        this.appointmentRepository = appointmentRepository;
        this.mongoTemplate = mongoTemplate;
        this.patientService = patientService;
    }

    public AppointmentResponse createAppointment(AppointmentRequest request) {
        PatientResponse patient = patientService.getPatientById(request.getPatientId());

        appointmentRepository.findByDoctorNameAndAppointmentDateAndAppointmentTime(
                        request.getDoctorName(), request.getAppointmentDate(), request.getAppointmentTime())
                .ifPresent(existing -> {
                    throw new InvalidStatusTransitionException(
                            "Doctor " + request.getDoctorName() + " already has an appointment at "
                                    + request.getAppointmentDate() + " " + request.getAppointmentTime());
                });
        appointmentRepository.findByPatientIdAndAppointmentDateAndAppointmentTime(
                        request.getPatientId(), request.getAppointmentDate(), request.getAppointmentTime())
                .ifPresent(existing -> {
                    throw new InvalidStatusTransitionException(
                            "Patient " + request.getPatientId() + " already has an appointment at "
                                    + request.getAppointmentDate() + " " + request.getAppointmentTime());
                });

        Appointment appointment = new Appointment();
        appointment.setPatientId(patient.getPatientId());
        appointment.setPatientName(patient.getName());        appointment.setDoctorName(request.getDoctorName());
        appointment.setDepartment(request.getDepartment());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Booked appointment {} for patient {}", saved.getId(), saved.getPatientId());
        return toResponse(saved);
    }

    public List<AppointmentResponse> getAppointments(String doctor, LocalDate date, Boolean upcoming) {
        Query query = new Query();
        if (doctor != null && !doctor.isBlank()) {
            query.addCriteria(Criteria.where("doctorName").is(doctor));
        }
        if (date != null) {
            query.addCriteria(Criteria.where("appointmentDate").is(date));
        }
        if (Boolean.TRUE.equals(upcoming)) {
            query.addCriteria(Criteria.where("appointmentDate").gte(LocalDate.now()));
            query.addCriteria(Criteria.where("status").is(Appointment.SCHEDULED));
            query.with(Sort.by(Sort.Direction.ASC, "appointmentDate").and(Sort.by(Sort.Direction.ASC, "appointmentTime")));
        } else {
            query.with(Sort.by(Sort.Direction.DESC, "appointmentDate"));
        }
        List<Appointment> appointments = mongoTemplate.find(query, Appointment.class);
        return appointments.stream().map(this::toResponse).toList();
    }

    public AppointmentResponse updateStatus(String id, String newStatus) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
        if (!Appointment.SCHEDULED.equals(appointment.getStatus())) {
            throw new InvalidStatusTransitionException(
                    "Appointment is already " + appointment.getStatus() + " and cannot be changed");
        }
        if (!Appointment.COMPLETED.equals(newStatus) && !Appointment.CANCELLED.equals(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Appointment status can only be changed to COMPLETED or CANCELLED");
        }
        appointment.setStatus(newStatus);
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment {} marked {}", saved.getId(), saved.getStatus());
        return toResponse(saved);
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setPatientId(appointment.getPatientId());
        response.setPatientName(appointment.getPatientName());
        response.setDoctorName(appointment.getDoctorName());
        response.setDepartment(appointment.getDepartment());
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setAppointmentTime(appointment.getAppointmentTime());
        response.setStatus(appointment.getStatus());
        response.setCreatedAt(appointment.getCreatedAt());
        return response;
    }
}
