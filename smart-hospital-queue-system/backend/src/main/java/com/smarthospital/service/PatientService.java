package com.smarthospital.service;

import com.smarthospital.dto.PatientRequest;
import com.smarthospital.dto.PatientResponse;
import com.smarthospital.dto.PageResponse;
import com.smarthospital.dto.WaitTimeResponse;
import com.smarthospital.entity.PatientStatus;

import java.util.List;

public interface PatientService {
    PatientResponse registerPatient(PatientRequest request);
    List<PatientResponse> getActiveQueue(String search, String department, String doctor, Boolean emergency, String status);
    PageResponse<PatientResponse> getHistory(String search, int page, int size);
    PageResponse<PatientResponse> getPatientHistory(String patientId, int page, int size);
    PatientResponse getPatientById(String patientId);
    PatientResponse updatePatient(String patientId, PatientRequest request);
    PatientResponse updateStatus(String patientId, PatientStatus status);
    WaitTimeResponse getEstimatedWaitingTime(String patientId);
    void deletePatient(String patientId);
}
