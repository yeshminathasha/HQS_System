package com.smarthospital.controller;

import com.smarthospital.dto.DoctorResponse;
import com.smarthospital.dto.RecommendResponse;
import com.smarthospital.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> getDoctors() {
        return ResponseEntity.ok(doctorService.getDoctorsWithWorkload());
    }

    @GetMapping("/recommend")
    public ResponseEntity<RecommendResponse> recommendDoctor() {
        return ResponseEntity.ok(doctorService.recommendDoctor());
    }
}
