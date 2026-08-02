package com.smarthospital.dto;

public class RecommendResponse {

    private String doctorName;
    private String department;
    private long queueCount;

    public RecommendResponse(String doctorName, String department, long queueCount) {
        this.doctorName = doctorName;
        this.department = department;
        this.queueCount = queueCount;
    }

    public String getDoctorName() { return doctorName; }
    public String getDepartment() { return department; }
    public long getQueueCount() { return queueCount; }
}
