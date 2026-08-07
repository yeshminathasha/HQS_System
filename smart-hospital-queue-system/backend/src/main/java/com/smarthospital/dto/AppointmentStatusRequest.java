package com.smarthospital.dto;

import com.smarthospital.entity.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public class AppointmentStatusRequest {

    @NotNull(message = "Status is required")
    private AppointmentStatus status;

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
}
