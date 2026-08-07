package com.smarthospital.controller;

import com.smarthospital.dto.PatientRequest;
import com.smarthospital.dto.PatientResponse;
import com.smarthospital.dto.PageResponse;
import com.smarthospital.dto.StatusUpdateRequest;
import com.smarthospital.dto.WaitTimeResponse;
import com.smarthospital.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<PatientResponse> registerPatient(@Valid @RequestBody PatientRequest request) {
        PatientResponse saved = patientService.registerPatient(request);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/queue")
    public ResponseEntity<List<PatientResponse>> getActiveQueue(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String doctor,
            @RequestParam(required = false) Boolean emergency,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(patientService.getActiveQueue(search, department, doctor, emergency, status));
    }

    @GetMapping("/history")
    public ResponseEntity<PageResponse<PatientResponse>> getHistory(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return ResponseEntity.ok(patientService.getHistory(search, page, size));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<PageResponse<PatientResponse>> getPatientHistory(
            @PathVariable("id") String patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return ResponseEntity.ok(patientService.getPatientHistory(patientId, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatient(@PathVariable("id") String patientId) {
        return ResponseEntity.ok(patientService.getPatientById(patientId));
    }

    @GetMapping("/{id}/wait-time")
    public ResponseEntity<WaitTimeResponse> getWaitTime(@PathVariable("id") String patientId) {
        return ResponseEntity.ok(patientService.getEstimatedWaitingTime(patientId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(@PathVariable("id") String patientId,
                                                         @Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(patientService.updatePatient(patientId, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PatientResponse> updateStatus(@PathVariable("id") String patientId,
                                                        @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(patientService.updateStatus(patientId, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable("id") String patientId) {
        patientService.deletePatient(patientId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
