package com.smarthospital.dto;

import com.smarthospital.entity.PatientStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private PatientStatus status;

    public PatientStatus getStatus() { return status; }
    public void setStatus(PatientStatus status) { this.status = status; }
}
