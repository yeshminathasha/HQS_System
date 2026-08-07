package com.smarthospital.service;

import com.smarthospital.dto.AppointmentRequest;
import com.smarthospital.dto.AppointmentResponse;
import com.smarthospital.dto.PageResponse;
import com.smarthospital.dto.PatientResponse;
import com.smarthospital.entity.Appointment;
import com.smarthospital.entity.AppointmentStatus;
import com.smarthospital.entity.Doctor;
import com.smarthospital.exception.AppointmentNotFoundException;
import com.smarthospital.exception.DoctorNotFoundException;
import com.smarthospital.exception.InvalidStatusTransitionException;
import com.smarthospital.repository.AppointmentRepository;
import com.smarthospital.repository.DoctorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);
    private static final int MAX_PAGE_SIZE = 100;

    private static final Map<DayOfWeek, String> DAY_ABBREVIATIONS = Map.of(
            DayOfWeek.MONDAY, "MON", DayOfWeek.TUESDAY, "TUE", DayOfWeek.WEDNESDAY, "WED",
            DayOfWeek.THURSDAY, "THU", DayOfWeek.FRIDAY, "FRI", DayOfWeek.SATURDAY, "SAT",
            DayOfWeek.SUNDAY, "SUN");

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final MongoTemplate mongoTemplate;
    private final PatientService patientService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              MongoTemplate mongoTemplate,
                              PatientService patientService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.mongoTemplate = mongoTemplate;
        this.patientService = patientService;
    }

    public AppointmentResponse createAppointment(AppointmentRequest request) {
        PatientResponse patient = patientService.getPatientById(request.getPatientId());
        Doctor doctor = validateBooking(request.getDoctorName(), request.getAppointmentDate(), request.getAppointmentTime());

        assertSlotFree(request.getDoctorName(), request.getPatientId(), request.getAppointmentDate(),
                request.getAppointmentTime(), null);

        Appointment appointment = new Appointment();
        applyRequest(appointment, patient, request, doctor);
        Appointment saved = saveOrConflict(appointment);
        log.info("Booked appointment {} for patient {}", saved.getId(), saved.getPatientId());
        return toResponse(saved);
    }

    public AppointmentResponse updateAppointment(String id, AppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new InvalidStatusTransitionException(
                    "Only scheduled appointments can be rescheduled; current status is " + appointment.getStatus());
        }
        Doctor doctor = validateBooking(request.getDoctorName(), request.getAppointmentDate(), request.getAppointmentTime());
        assertSlotFree(request.getDoctorName(), appointment.getPatientId(), request.getAppointmentDate(),
                request.getAppointmentTime(), id);

        applyRequest(appointment, patientService.getPatientById(appointment.getPatientId()), request, doctor);
        Appointment saved = saveOrConflict(appointment);
        log.info("Rescheduled appointment {} for patient {}", saved.getId(), saved.getPatientId());
        return toResponse(saved);
    }

    public PageResponse<AppointmentResponse> getAppointments(String doctor, LocalDate date, Boolean upcoming,
                                                             String patientId, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
        Query query = new Query();
        if (doctor != null && !doctor.isBlank()) {
            query.addCriteria(Criteria.where("doctorName").is(doctor));
        }
        if (date != null) {
            query.addCriteria(Criteria.where("appointmentDate").is(date));
        }
        if (patientId != null && !patientId.isBlank()) {
            query.addCriteria(Criteria.where("patientId").is(patientId));
        }
        if (Boolean.TRUE.equals(upcoming)) {
            query.addCriteria(Criteria.where("appointmentDate").gte(LocalDate.now()));
            query.addCriteria(Criteria.where("status").is(AppointmentStatus.SCHEDULED));
            query.with(Sort.by(Sort.Direction.ASC, "appointmentDate").and(Sort.by(Sort.Direction.ASC, "appointmentTime")));
        } else {
            query.with(Sort.by(Sort.Direction.DESC, "appointmentDate"));
        }
        long total = mongoTemplate.count(query, Appointment.class);
        query.skip((long) safePage * safeSize).limit(safeSize);
        List<Appointment> appointments = mongoTemplate.find(query, Appointment.class);
        int totalPages = (int) Math.ceil((double) total / safeSize);
        return new PageResponse<>(appointments.stream().map(this::toResponse).toList(),
                safePage, safeSize, total, totalPages);
    }

    public AppointmentResponse updateStatus(String id, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
        if (!AppointmentStatus.SCHEDULED.equals(appointment.getStatus())) {
            throw new InvalidStatusTransitionException(
                    "Appointment is already " + appointment.getStatus() + " and cannot be changed");
        }
        if (!AppointmentStatus.COMPLETED.equals(newStatus) && !AppointmentStatus.CANCELLED.equals(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Appointment status can only be changed to COMPLETED or CANCELLED");
        }
        appointment.setStatus(newStatus);
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment {} marked {}", saved.getId(), saved.getStatus());
        return toResponse(saved);
    }

    private Doctor validateBooking(String doctorName, LocalDate date, String time) {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Appointment date cannot be in the past");
        }
        Doctor doctor = doctorRepository.findByName(doctorName)
                .orElseThrow(() -> new DoctorNotFoundException(doctorName));
        List<String> workDays = doctor.getWorkDays();
        if (workDays != null && !workDays.isEmpty()) {
            String dayAbbr = DAY_ABBREVIATIONS.get(date.getDayOfWeek());
            if (!workDays.contains(dayAbbr)) {
                throw new IllegalArgumentException(
                        "Dr. " + doctorName + " does not work on " + date.getDayOfWeek().toString().toLowerCase());
            }
        }
        if (doctor.getStartTime() != null && doctor.getEndTime() != null
                && (time.compareTo(doctor.getStartTime()) < 0 || time.compareTo(doctor.getEndTime()) >= 0)) {
            throw new IllegalArgumentException(
                    "Dr. " + doctorName + " works between " + doctor.getStartTime() + " and " + doctor.getEndTime());
        }
        return doctor;
    }

    private void assertSlotFree(String doctorName, String patientId, LocalDate date, String time, String excludeId) {
        appointmentRepository.findByDoctorNameAndAppointmentDateAndAppointmentTimeAndStatus(
                        doctorName, date, time, AppointmentStatus.SCHEDULED)
                .ifPresent(existing -> {
                    if (excludeId == null || !excludeId.equals(existing.getId())) {
                        throw new InvalidStatusTransitionException(
                                "Doctor " + doctorName + " already has a scheduled appointment at "
                                        + date + " " + time);
                    }
                });
        appointmentRepository.findByPatientIdAndAppointmentDateAndAppointmentTimeAndStatus(
                        patientId, date, time, AppointmentStatus.SCHEDULED)
                .ifPresent(existing -> {
                    if (excludeId == null || !excludeId.equals(existing.getId())) {
                        throw new InvalidStatusTransitionException(
                                "Patient " + patientId + " already has a scheduled appointment at "
                                        + date + " " + time);
                    }
                });
    }

    private void applyRequest(Appointment appointment, PatientResponse patient,
                              AppointmentRequest request, Doctor doctor) {
        appointment.setPatientId(patient.getPatientId());
        appointment.setPatientName(patient.getName());
        appointment.setDoctorName(request.getDoctorName());
        appointment.setDepartment(doctor.getDepartment());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
    }

    private Appointment saveOrConflict(Appointment appointment) {
        try {
            return appointmentRepository.save(appointment);
        } catch (DuplicateKeyException ex) {
            throw new InvalidStatusTransitionException(
                    "This time slot was just booked by another request; please pick a different time");
        }
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
