package com.smarthospital.service;

import com.smarthospital.dto.PatientRequest;
import com.smarthospital.dto.PatientResponse;
import com.smarthospital.dto.PageResponse;
import com.smarthospital.dto.WaitTimeResponse;
import com.smarthospital.entity.Patient;
import com.smarthospital.entity.PatientStatus;
import com.smarthospital.exception.InvalidStatusTransitionException;
import com.smarthospital.exception.PatientNotFoundException;
import com.smarthospital.mapper.PatientMapper;
import com.smarthospital.repository.DoctorRepository;
import com.smarthospital.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PatientServiceImpl implements PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientServiceImpl.class);
    private static final List<PatientStatus> ACTIVE_STATUSES =
            List.of(PatientStatus.WAITING, PatientStatus.IN_CONSULTATION);
    private static final List<PatientStatus> HISTORY_STATUSES =
            List.of(PatientStatus.COMPLETED, PatientStatus.CANCELLED);
    private static final int MAX_PAGE_SIZE = 100;

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final MongoTemplate mongoTemplate;
    private final SequenceService sequenceService;
    private final PatientMapper patientMapper;
    private final int avgServiceMinutes;

    public PatientServiceImpl(PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              MongoTemplate mongoTemplate,
                              SequenceService sequenceService,
                              PatientMapper patientMapper,
                              @Value("${queue.avg-service-minutes:15}") int avgServiceMinutes) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.mongoTemplate = mongoTemplate;
        this.sequenceService = sequenceService;
        this.patientMapper = patientMapper;
        this.avgServiceMinutes = avgServiceMinutes;
    }

    @Override
    public PatientResponse registerPatient(PatientRequest request) {
        Patient patient = patientMapper.toEntity(request);
        validateDoctor(request.getDoctorName());
        patient.setPatientId("P" + String.format("%03d", sequenceService.nextValue("patientId")));
        applyPriorityRules(patient);
        Patient saved = patientRepository.save(patient);
        log.info("Registered patient {} ({})", saved.getPatientId(), saved.getName());
        return patientMapper.toResponse(saved);
    }

    @Override
    public List<PatientResponse> getActiveQueue(String search, String department, String doctor, Boolean emergency, String status) {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").in(ACTIVE_STATUSES));
        if (status != null && !status.isBlank()) {
            query.addCriteria(Criteria.where("status").is(PatientStatus.valueOf(status)));
        }
        applyOptionalFilters(query, search, department, doctor, emergency);
        query.with(queueSort());
        return toResponses(mongoTemplate.find(query, Patient.class));
    }

    @Override
    public PageResponse<PatientResponse> getHistory(String search, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
        Query query = new Query();
        query.addCriteria(Criteria.where("status").in(HISTORY_STATUSES));
        applyOptionalFilters(query, search, null, null, null);
        long total = mongoTemplate.count(query, Patient.class);
        query.with(Sort.by(Sort.Direction.DESC, "registeredAt"));
        query.skip((long) safePage * safeSize).limit(safeSize);
        List<PatientResponse> content = toResponses(mongoTemplate.find(query, Patient.class));
        int totalPages = (int) Math.ceil((double) total / safeSize);
        return new PageResponse<>(content, safePage, safeSize, total, totalPages);
    }

    @Override
    public PageResponse<PatientResponse> getPatientHistory(String patientId, int page, int size) {
        getPatient(patientId);
        int safePage = Math.max(0, page);
        int safeSize = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
        Query query = new Query();
        query.addCriteria(Criteria.where("patientId").is(patientId));
        query.addCriteria(Criteria.where("status").in(HISTORY_STATUSES));
        long total = mongoTemplate.count(query, Patient.class);
        query.with(Sort.by(Sort.Direction.DESC, "registeredAt"));
        query.skip((long) safePage * safeSize).limit(safeSize);
        List<PatientResponse> content = toResponses(mongoTemplate.find(query, Patient.class));
        int totalPages = (int) Math.ceil((double) total / safeSize);
        return new PageResponse<>(content, safePage, safeSize, total, totalPages);
    }

    @Override
    public PatientResponse getPatientById(String patientId) {
        return patientMapper.toResponse(getPatient(patientId));
    }

    @Override
    public PatientResponse updatePatient(String patientId, PatientRequest request) {
        Patient existing = getPatient(patientId);
        String nextDoctor = request.getDoctorName() != null ? request.getDoctorName() : existing.getDoctorName();
        validateDoctor(nextDoctor);
        if (request.getName() != null) existing.setName(request.getName());
        if (request.getContactNumber() != null) existing.setContactNumber(request.getContactNumber());
        if (request.getDepartment() != null) existing.setDepartment(request.getDepartment());
        if (request.getDoctorName() != null) existing.setDoctorName(request.getDoctorName());
        if (request.getPriorityLevel() != null) existing.setPriorityLevel(request.getPriorityLevel());
        existing.setEmergency(request.isEmergency());
        applyPriorityRules(existing);
        Patient saved = patientRepository.save(existing);
        syncAppointmentDetails(saved);
        log.info("Updated patient {}", saved.getPatientId());
        return patientMapper.toResponse(saved);
    }

    @Override
    public PatientResponse updateStatus(String patientId, PatientStatus newStatus) {
        Patient patient = getPatient(patientId);
        PatientStatus current = patient.getStatus();
        if (current == newStatus) {
            return patientMapper.toResponse(patient);
        }
        switch (current) {
            case WAITING -> {
                if (newStatus != PatientStatus.IN_CONSULTATION && newStatus != PatientStatus.CANCELLED) {
                    throw invalidTransition(current, newStatus);
                }
            }
            case IN_CONSULTATION -> {
                if (newStatus != PatientStatus.COMPLETED && newStatus != PatientStatus.CANCELLED) {
                    throw invalidTransition(current, newStatus);
                }
            }
            default -> throw invalidTransition(current, newStatus);
        }
        patient.setStatus(newStatus);
        if (newStatus == PatientStatus.COMPLETED) {
            patient.setCompletedAt(LocalDateTime.now());
            patient.setWaitMinutes(Math.max(0, Duration.between(patient.getRegisteredAt(), patient.getCompletedAt()).toMinutes()));
        }
        patientRepository.save(patient);
        log.info("Patient {} moved from {} to {}", patient.getPatientId(), current, newStatus);
        return patientMapper.toResponse(patient);
    }

    @Override
    public WaitTimeResponse getEstimatedWaitingTime(String patientId) {
        List<Patient> active = getSortedActiveQueue();
        for (int i = 0; i < active.size(); i++) {
            if (active.get(i).getPatientId().equalsIgnoreCase(patientId)) {
                return new WaitTimeResponse(patientId, i + 1, i, (long) i * avgServiceMinutes);
            }
        }
        throw new PatientNotFoundException(patientId + " is not in the active queue");
    }

    @Override
    public void deletePatient(String patientId) {
        Patient patient = getPatient(patientId);
        if (patient.getStatus() == PatientStatus.WAITING || patient.getStatus() == PatientStatus.IN_CONSULTATION) {
            throw new InvalidStatusTransitionException(
                    "Patient " + patientId + " is active; cancel the appointment before deleting");
        }
        patientRepository.delete(patient);
        log.info("Deleted patient {}", patientId);
    }

    Patient getPatient(String patientId) {
        return patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));
    }

    List<Patient> getSortedActiveQueue() {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").in(ACTIVE_STATUSES));
        query.with(queueSort());
        return mongoTemplate.find(query, Patient.class);
    }

    private Sort queueSort() {
        return Sort.by(Sort.Direction.DESC, "emergency")
                .and(Sort.by(Sort.Direction.ASC, "priorityLevel"))
                .and(Sort.by(Sort.Direction.ASC, "registeredAt"));
    }

    private void applyOptionalFilters(Query query, String search, String department, String doctor, Boolean emergency) {
        if (search != null && !search.isBlank()) {
            Pattern pattern = Pattern.compile(Pattern.quote(search.trim()), Pattern.CASE_INSENSITIVE);
            List<Criteria> ors = new ArrayList<>();
            ors.add(Criteria.where("name").regex(pattern));
            ors.add(Criteria.where("patientId").regex(pattern));
            ors.add(Criteria.where("doctorName").regex(pattern));
            ors.add(Criteria.where("department").regex(pattern));
            query.addCriteria(new Criteria().orOperator(ors.toArray(new Criteria[0])));
        }
        if (department != null && !department.isBlank()) {
            query.addCriteria(Criteria.where("department").is(department));
        }
        if (doctor != null && !doctor.isBlank()) {
            query.addCriteria(Criteria.where("doctorName").is(doctor));
        }
        if (emergency != null) {
            query.addCriteria(Criteria.where("emergency").is(emergency));
        }
    }

    private void applyPriorityRules(Patient patient) {
        if (patient.isEmergency()) {
            int priority = patient.getPriorityLevel();
            if (priority < 1 || priority > 3) {
                throw new IllegalArgumentException("Priority level must be between 1 and 3 for emergency cases");
            }
        } else {
            patient.setPriorityLevel(0);
        }
    }

    private void validateDoctor(String doctorName) {
        doctorRepository.findByName(doctorName)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorName));
    }

    private void syncAppointmentDetails(Patient patient) {
        Query query = new Query(Criteria.where("patientId").is(patient.getPatientId()));
        Update update = new Update()
                .set("patientName", patient.getName())
                .set("department", patient.getDepartment());
        mongoTemplate.updateMulti(query, update, "appointments");
    }

    private InvalidStatusTransitionException invalidTransition(PatientStatus current, PatientStatus target) {
        return new InvalidStatusTransitionException(
                "Invalid status transition from " + current + " to " + target);
    }

    private List<PatientResponse> toResponses(List<Patient> patients) {
        return patients.stream().map(patientMapper::toResponse).toList();
    }
}
