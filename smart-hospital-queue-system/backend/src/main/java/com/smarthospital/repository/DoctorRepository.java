package com.smarthospital.repository;

import com.smarthospital.entity.Doctor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DoctorRepository extends MongoRepository<Doctor, String> {
    Optional<Doctor> findByName(String name);
}
