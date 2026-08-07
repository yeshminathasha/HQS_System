package com.smarthospital.service;

import com.smarthospital.dto.PatientRequest;
import com.smarthospital.dto.PatientResponse;
import com.smarthospital.dto.WaitTimeResponse;
import com.smarthospital.entity.Doctor;
import com.smarthospital.entity.Patient;
import com.smarthospital.entity.PatientStatus;
import com.smarthospital.exception.InvalidStatusTransitionException;
import com.smarthospital.exception.PatientNotFoundException;
import com.smarthospital.mapper.PatientMapper;
import com.smarthospital.repository.DoctorRepository;
import com.smarthospital.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private SequenceService sequenceService;

    private final PatientMapper mapper = Mappers.getMapper(PatientMapper.class);
    private PatientServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PatientServiceImpl(patientRepository, doctorRepository, mongoTemplate, sequenceService, mapper, 15);
    }

    private PatientRequest request(String name, boolean emergency, Integer priority) {
        PatientRequest r = new PatientRequest();
        r.setName(name);
        r.setContactNumber("0771234567");
        r.setDepartment("General");
        r.setDoctorName("Dr. Adams");
        r.setEmergency(emergency);
        r.setPriorityLevel(priority);
        return r;
    }

    @Test
    void registerGeneratesSequentialPatientId() {
        when(doctorRepository.findByName("Dr. Adams")).thenReturn(Optional.of(new Doctor()));
        when(sequenceService.nextValue("patientId")).thenReturn(7L);
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        PatientResponse response = service.registerPatient(request("John Doe", false, null));

        assertEquals("P007", response.getPatientId());
        assertEquals(PatientStatus.WAITING, response.getStatus());
        assertNotNull(response.getRegisteredAt());
    }

    @Test
    void registerEmergencyWithoutValidPriorityThrows() {
        when(doctorRepository.findByName("Dr. Adams")).thenReturn(Optional.of(new Doctor()));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registerPatient(request("Urgent", true, 5)));
        assertTrue(ex.getMessage().contains("1 and 3"));
        verify(patientRepository, never()).save(any());
    }

    @Test
    void registerNonEmergencyForcesZeroPriority() {
        when(doctorRepository.findByName("Dr. Adams")).thenReturn(Optional.of(new Doctor()));
        when(sequenceService.nextValue("patientId")).thenReturn(1L);
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        PatientResponse response = service.registerPatient(request("Normal", false, 2));

        assertEquals(0, response.getPriorityLevel());
    }

    @Test
    void registerWithUnknownDoctorThrows() {
        when(doctorRepository.findByName("Dr. Nobody")).thenReturn(Optional.empty());

        PatientRequest r = request("John Doe", false, null);
        r.setDoctorName("Dr. Nobody");

        assertThrows(IllegalArgumentException.class, () -> service.registerPatient(r));
        verify(patientRepository, never()).save(any());
    }

    @Test
    void getActiveQueueUsesActiveStatusesAndPrioritySort() {
        Patient waiting = patient("P001", PatientStatus.WAITING, false, 0);
        when(mongoTemplate.find(any(Query.class), eq(Patient.class))).thenReturn(List.of(waiting));

        service.getActiveQueue(null, null, null, null, null);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Patient.class));
        Query query = captor.getValue();

        var sort = query.getSortObject();
        assertEquals(-1, sort.getInteger("emergency"));
        assertEquals(1, sort.getInteger("priorityLevel"));
        assertEquals(1, sort.getInteger("registeredAt"));

        var status = query.getQueryObject().get("status");
        assertNotNull(status);
    }

    @Test
    void updateStatusWaitingToConsultationSucceeds() {
        Patient patient = patient("P001", PatientStatus.WAITING, false, 0);
        when(patientRepository.findByPatientId("P001")).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        PatientResponse response = service.updateStatus("P001", PatientStatus.IN_CONSULTATION);

        assertEquals(PatientStatus.IN_CONSULTATION, response.getStatus());
    }

    @Test
    void completeComputesWaitMinutes() {
        Patient patient = patient("P001", PatientStatus.IN_CONSULTATION, false, 0);
        patient.setRegisteredAt(LocalDateTime.now().minusMinutes(30));
        when(patientRepository.findByPatientId("P001")).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        PatientResponse response = service.updateStatus("P001", PatientStatus.COMPLETED);

        assertEquals(PatientStatus.COMPLETED, response.getStatus());
        assertTrue(response.getWaitMinutes() >= 29 && response.getWaitMinutes() <= 31);
        assertNotNull(response.getCompletedAt());
    }

    @Test
    void invalidTransitionThrows() {
        Patient patient = patient("P001", PatientStatus.IN_CONSULTATION, false, 0);
        when(patientRepository.findByPatientId("P001")).thenReturn(Optional.of(patient));

        assertThrows(InvalidStatusTransitionException.class,
                () -> service.updateStatus("P001", PatientStatus.WAITING));
    }

    @Test
    void completedIsTerminal() {
        Patient patient = patient("P001", PatientStatus.COMPLETED, false, 0);
        when(patientRepository.findByPatientId("P001")).thenReturn(Optional.of(patient));

        assertThrows(InvalidStatusTransitionException.class,
                () -> service.updateStatus("P001", PatientStatus.CANCELLED));
    }

    @Test
    void cancelWorks() {
        Patient patient = patient("P001", PatientStatus.WAITING, false, 0);
        when(patientRepository.findByPatientId("P001")).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        PatientResponse response = service.updateStatus("P001", PatientStatus.CANCELLED);

        assertEquals(PatientStatus.CANCELLED, response.getStatus());
    }

    @Test
    void getPatientByIdNotFoundThrows() {
        when(patientRepository.findByPatientId("P999")).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> service.getPatientById("P999"));
    }

    @Test
    void deleteActivePatientRejected() {
        Patient patient = patient("P001", PatientStatus.WAITING, false, 0);
        when(patientRepository.findByPatientId("P001")).thenReturn(Optional.of(patient));

        assertThrows(InvalidStatusTransitionException.class, () -> service.deletePatient("P001"));
        verify(patientRepository, never()).delete(any());
    }

    @Test
    void deleteCancelledPatientSucceeds() {
        Patient patient = patient("P001", PatientStatus.CANCELLED, false, 0);
        when(patientRepository.findByPatientId("P001")).thenReturn(Optional.of(patient));
        doNothing().when(patientRepository).delete(patient);

        service.deletePatient("P001");

        verify(patientRepository).delete(patient);
    }

    @Test
    void waitingTimeUsesPositionInPriorityOrder() {
        Patient normal = patient("P001", PatientStatus.WAITING, false, 0);
        Patient urgent = patient("P002", PatientStatus.WAITING, true, 1);
        when(mongoTemplate.find(any(Query.class), eq(Patient.class))).thenReturn(List.of(urgent, normal));

        WaitTimeResponse response = service.getEstimatedWaitingTime("P002");

        assertEquals(1, response.getPosition());
        assertEquals(0, response.getPatientsAhead());
        assertEquals(0, response.getEstimatedMinutes());
    }

    @Test
    void waitingTimeForPatientNotInQueueThrows() {
        Patient normal = patient("P001", PatientStatus.WAITING, false, 0);
        when(mongoTemplate.find(any(Query.class), eq(Patient.class))).thenReturn(List.of(normal));

        assertThrows(PatientNotFoundException.class, () -> service.getEstimatedWaitingTime("P999"));
    }

    private Patient patient(String id, PatientStatus status, boolean emergency, int priority) {
        Patient p = new Patient();
        p.setPatientId(id);
        p.setName("Test Patient");
        p.setContactNumber("0771234567");
        p.setDepartment("General");
        p.setDoctorName("Dr. Adams");
        p.setEmergency(emergency);
        p.setPriorityLevel(priority);
        p.setStatus(status);
        return p;
    }
}
