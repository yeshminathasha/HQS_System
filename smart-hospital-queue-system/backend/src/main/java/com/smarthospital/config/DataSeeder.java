package com.smarthospital.config;

import com.smarthospital.entity.Doctor;
import com.smarthospital.repository.DoctorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final DoctorRepository doctorRepository;

    public DataSeeder(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    public void run(String... args) {
        if (doctorRepository.count() > 0) {
            return;
        }
        List<Doctor> doctors = List.of(
                doctor("Dr. Smith", "Cardiology", List.of("MON", "TUE", "WED", "THU", "FRI"), "09:00", "17:00"),
                doctor("Dr. Jones", "Neurology", List.of("MON", "TUE", "THU", "FRI"), "09:00", "16:00"),
                doctor("Dr. Adams", "General", List.of("MON", "TUE", "WED", "THU", "FRI", "SAT"), "08:00", "18:00"),
                doctor("Dr. Patel", "Orthopedics", List.of("TUE", "WED", "THU", "FRI"), "10:00", "17:00"));
        doctorRepository.saveAll(doctors);
        log.info("Seeded {} demo doctors", doctors.size());
    }

    private Doctor doctor(String name, String department, List<String> workDays, String start, String end) {
        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setDepartment(department);
        doctor.setWorkDays(workDays);
        doctor.setStartTime(start);
        doctor.setEndTime(end);
        return doctor;
    }
}
