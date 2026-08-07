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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private PatientService patientService;

    private AppointmentService service;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        service = new AppointmentService(appointmentRepository, doctorRepository, mongoTemplate, patientService);
        doctor = new Doctor();
        doctor.setName("Dr. Smith");
        doctor.setDepartment("Cardiology");
        doctor.setWorkDays(List.of("MON", "TUE", "WED", "THU", "FRI"));
        doctor.setStartTime("09:00");
        doctor.setEndTime("17:00");
    }

    private AppointmentRequest request(LocalDate date, String time) {
        AppointmentRequest r = new AppointmentRequest();
        r.setPatientId("P001");
        r.setDoctorName("Dr. Smith");
        r.setDepartment("Cardiology");
        r.setAppointmentDate(date);
        r.setAppointmentTime(time);
        return r;
    }

    private LocalDate nextWeekday() {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    private LocalDate nextSaturday() {
        return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
    }

    private PatientResponse patient() {
        PatientResponse p = new PatientResponse();
        p.setPatientId("P001");
        p.setName("John Doe");
        return p;
    }

    private void stubValid() {
        when(patientService.getPatientById("P001")).thenReturn(patient());
        when(doctorRepository.findByName("Dr. Smith")).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctorNameAndAppointmentDateAndAppointmentTimeAndStatus(
                any(), any(), any(), eq(AppointmentStatus.SCHEDULED))).thenReturn(Optional.empty());
        when(appointmentRepository.findByPatientIdAndAppointmentDateAndAppointmentTimeAndStatus(
                any(), any(), any(), eq(AppointmentStatus.SCHEDULED))).thenReturn(Optional.empty());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createAppointmentSucceedsAndUsesDoctorDepartment() {
        stubValid();
        LocalDate date = nextWeekday();

        AppointmentResponse response = service.createAppointment(request(date, "10:00"));

        assertEquals("P001", response.getPatientId());
        assertEquals("John Doe", response.getPatientName());
        assertEquals("Cardiology", response.getDepartment());
        assertEquals("Dr. Smith", response.getDoctorName());
        assertEquals(AppointmentStatus.SCHEDULED, response.getStatus());
    }

    @Test
    void createRejectsDoubleBookedDoctorSlot() {
        when(patientService.getPatientById("P001")).thenReturn(patient());
        when(doctorRepository.findByName("Dr. Smith")).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctorNameAndAppointmentDateAndAppointmentTimeAndStatus(
                any(), any(), any(), eq(AppointmentStatus.SCHEDULED)))
                .thenReturn(Optional.of(new Appointment()));

        assertThrows(InvalidStatusTransitionException.class,
                () -> service.createAppointment(request(nextWeekday(), "10:00")));
    }

    @Test
    void createRejectsDoubleBookedPatientSlot() {
        when(patientService.getPatientById("P001")).thenReturn(patient());
        when(doctorRepository.findByName("Dr. Smith")).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctorNameAndAppointmentDateAndAppointmentTimeAndStatus(
                any(), any(), any(), eq(AppointmentStatus.SCHEDULED))).thenReturn(Optional.empty());
        when(appointmentRepository.findByPatientIdAndAppointmentDateAndAppointmentTimeAndStatus(
                any(), any(), any(), eq(AppointmentStatus.SCHEDULED)))
                .thenReturn(Optional.of(new Appointment()));

        assertThrows(InvalidStatusTransitionException.class,
                () -> service.createAppointment(request(nextWeekday(), "10:00")));
    }

    @Test
    void createRejectsPastDate() {
        when(patientService.getPatientById("P001")).thenReturn(patient());
        LocalDate yesterday = LocalDate.now().minusDays(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createAppointment(request(yesterday, "10:00")));
        assertTrue(ex.getMessage().contains("past"));
    }

    @Test
    void createRejectsUnknownDoctor() {
        when(patientService.getPatientById("P001")).thenReturn(patient());
        when(doctorRepository.findByName("Dr. Smith")).thenReturn(Optional.empty());
        AppointmentRequest r = request(nextWeekday(), "10:00");

        assertThrows(DoctorNotFoundException.class, () -> service.createAppointment(r));
    }

    @Test
    void createRejectsDayOff() {
        when(patientService.getPatientById("P001")).thenReturn(patient());
        when(doctorRepository.findByName("Dr. Smith")).thenReturn(Optional.of(doctor));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createAppointment(request(nextSaturday(), "10:00")));
        assertTrue(ex.getMessage().contains("does not work"));
    }

    @Test
    void createRejectsOutsideWorkingHours() {
        when(patientService.getPatientById("P001")).thenReturn(patient());
        when(doctorRepository.findByName("Dr. Smith")).thenReturn(Optional.of(doctor));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createAppointment(request(nextWeekday(), "18:00")));
        assertTrue(ex.getMessage().contains("works between"));
    }

    @Test
    void cancelledSlotCanBeRebooked() {
        stubValid();

        AppointmentResponse response = service.createAppointment(request(nextWeekday(), "10:00"));

        assertEquals("10:00", response.getAppointmentTime());
        verify(appointmentRepository, never()).save(null);
    }

    @Test
    void updateAppointmentReschedulesScheduled() {
        Appointment existing = new Appointment();
        existing.setId("a1");
        existing.setPatientId("P001");
        existing.setStatus(AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById("a1")).thenReturn(Optional.of(existing));
        when(patientService.getPatientById("P001")).thenReturn(patient());
        when(doctorRepository.findByName("Dr. Smith")).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctorNameAndAppointmentDateAndAppointmentTimeAndStatus(
                any(), any(), any(), eq(AppointmentStatus.SCHEDULED))).thenReturn(Optional.empty());
        when(appointmentRepository.findByPatientIdAndAppointmentDateAndAppointmentTimeAndStatus(
                any(), any(), any(), eq(AppointmentStatus.SCHEDULED))).thenReturn(Optional.empty());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = service.updateAppointment("a1", request(nextWeekday(), "14:00"));

        assertEquals("14:00", response.getAppointmentTime());
        assertEquals("Cardiology", response.getDepartment());
    }

    @Test
    void updateAppointmentIgnoresSelfInConflictCheck() {
        Appointment existing = new Appointment();
        existing.setId("a1");
        existing.setPatientId("P001");
        existing.setStatus(AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById("a1")).thenReturn(Optional.of(existing));
        when(patientService.getPatientById("P001")).thenReturn(patient());
        when(doctorRepository.findByName("Dr. Smith")).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctorNameAndAppointmentDateAndAppointmentTimeAndStatus(
                any(), any(), any(), eq(AppointmentStatus.SCHEDULED))).thenReturn(Optional.of(existing));
        when(appointmentRepository.findByPatientIdAndAppointmentDateAndAppointmentTimeAndStatus(
                any(), any(), any(), eq(AppointmentStatus.SCHEDULED))).thenReturn(Optional.empty());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = service.updateAppointment("a1", request(nextWeekday(), "14:00"));

        assertEquals("14:00", response.getAppointmentTime());
    }

    @Test
    void updateAppointmentRejectsCompleted() {
        Appointment existing = new Appointment();
        existing.setId("a1");
        existing.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById("a1")).thenReturn(Optional.of(existing));

        assertThrows(InvalidStatusTransitionException.class,
                () -> service.updateAppointment("a1", request(nextWeekday(), "14:00")));
    }

    @Test
    void updateAppointmentNotFoundThrows() {
        when(appointmentRepository.findById("nope")).thenReturn(Optional.empty());

        assertThrows(AppointmentNotFoundException.class,
                () -> service.updateAppointment("nope", request(nextWeekday(), "14:00")));
    }

    @Test
    void saveConflictMapsToDomainException() {
        when(patientService.getPatientById("P001")).thenReturn(patient());
        when(doctorRepository.findByName("Dr. Smith")).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctorNameAndAppointmentDateAndAppointmentTimeAndStatus(
                any(), any(), any(), eq(AppointmentStatus.SCHEDULED))).thenReturn(Optional.empty());
        when(appointmentRepository.findByPatientIdAndAppointmentDateAndAppointmentTimeAndStatus(
                any(), any(), any(), eq(AppointmentStatus.SCHEDULED))).thenReturn(Optional.empty());
        when(appointmentRepository.save(any(Appointment.class))).thenThrow(new DuplicateKeyException("dup"));

        assertThrows(InvalidStatusTransitionException.class,
                () -> service.createAppointment(request(nextWeekday(), "10:00")));
    }

    @Test
    void updateStatusOnlyAllowsCompleteOrCancelOnScheduled() {
        Appointment scheduled = new Appointment();
        scheduled.setId("a1");
        scheduled.setStatus(AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById("a1")).thenReturn(Optional.of(scheduled));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(AppointmentStatus.COMPLETED, service.updateStatus("a1", AppointmentStatus.COMPLETED).getStatus());

        Appointment completed = new Appointment();
        completed.setId("a2");
        completed.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById("a2")).thenReturn(Optional.of(completed));
        assertThrows(InvalidStatusTransitionException.class,
                () -> service.updateStatus("a2", AppointmentStatus.CANCELLED));
    }

    @Test
    void getAppointmentsAppliesPagination() {
        Appointment appointment = new Appointment();
        appointment.setId("a1");
        when(mongoTemplate.count(any(Query.class), eq(Appointment.class))).thenReturn(30L);
        when(mongoTemplate.find(any(Query.class), eq(Appointment.class)))
                .thenReturn(List.of(appointment));

        PageResponse<AppointmentResponse> page = service.getAppointments(null, null, true, null, 1, 25);

        assertEquals(2, page.totalPages());
        assertEquals(30L, page.totalElements());
        assertEquals(1, page.content().size());
        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Appointment.class));
        assertEquals(25, captor.getValue().getSkip());
        assertEquals(25, captor.getValue().getLimit());
    }

    @Test
    void getAppointmentsFiltersByPatient() {
        when(mongoTemplate.count(any(Query.class), eq(Appointment.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Appointment.class))).thenReturn(List.of());

        service.getAppointments(null, null, null, "P001", 0, 25);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Appointment.class));
        assertEquals("P001", captor.getValue().getQueryObject().get("patientId"));
    }
}
