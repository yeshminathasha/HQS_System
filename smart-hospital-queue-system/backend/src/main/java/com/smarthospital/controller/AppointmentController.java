package com.smarthospital.controller;

import com.smarthospital.dto.AppointmentRequest;
import com.smarthospital.dto.AppointmentResponse;
import com.smarthospital.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import com.smarthospital.security.CustomUserDetails;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse created = appointmentService.createAppointment(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAppointments(
            @RequestParam(required = false) String doctor,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Boolean upcoming) {
        return ResponseEntity.ok(appointmentService.getAppointments(doctor, date, upcoming));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(@PathVariable("id") String id,
                                                            @RequestBody AppointmentStatusRequest request) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, request.getStatus()));
    }

    public static class AppointmentStatusRequest {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    
   @GetMapping("/mine")
    public List<AppointmentResponseDto> getMyAppointments(Authentication authentication) {
    String userId = ((CustomUserDetails) authentication.getPrincipal()).getUser().getId();
    return appointmentService.getAppointmentsForUser(userId);
}


    @PostMapping
    public AppointmentResponseDto book(@Valid @RequestBody AppointmentRequestDto dto,
                                    Authentication authentication) {
    CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
    boolean isUser = principal.getUser().getRole().name().equals("USER");

    String bookedByUserId = isUser ? principal.getUser().getId() : null;

    return appointmentService.bookAppointment(dto, bookedByUserId);
}


    @PatchMapping("/{id}/status")
    public AppointmentResponseDto updateStatus(@PathVariable String id,
                                            @RequestBody StatusUpdateDto dto,
                                            Authentication authentication) {
    CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
    boolean isUser = principal.getUser().getRole().name().equals("USER");

    if (isUser) {
    
        return appointmentService.updateStatusAsOwner(id, dto.getStatus(), principal.getUser().getId());
    }
    return appointmentService.updateStatus(id, dto.getStatus());
}


}
