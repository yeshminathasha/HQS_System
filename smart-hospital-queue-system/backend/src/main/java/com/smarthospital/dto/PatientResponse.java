package com.smarthospital.dto;

import com.smarthospital.entity.PatientStatus;

import java.time.LocalDateTime;

public class PatientResponse {

    private String id;
    private String patientId;
    private String name;
    private String contactNumber;
    private String department;
    private String doctorName;
    private boolean emergency;
    private int priorityLevel;
    private PatientStatus status;
    private LocalDateTime registeredAt;
    private LocalDateTime completedAt;
    private long waitMinutes;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public boolean isEmergency() { return emergency; }
    public void setEmergency(boolean emergency) { this.emergency = emergency; }

    public int getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(int priorityLevel) { this.priorityLevel = priorityLevel; }

    public PatientStatus getStatus() { return status; }
    public void setStatus(PatientStatus status) { this.status = status; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public long getWaitMinutes() { return waitMinutes; }
    public void setWaitMinutes(long waitMinutes) { this.waitMinutes = waitMinutes; }
}
