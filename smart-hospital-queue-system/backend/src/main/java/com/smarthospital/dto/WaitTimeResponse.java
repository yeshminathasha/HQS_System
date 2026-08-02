package com.smarthospital.dto;

public class WaitTimeResponse {

    private String patientId;
    private int position;
    private int patientsAhead;
    private long estimatedMinutes;

    public WaitTimeResponse(String patientId, int position, int patientsAhead, long estimatedMinutes) {
        this.patientId = patientId;
        this.position = position;
        this.patientsAhead = patientsAhead;
        this.estimatedMinutes = estimatedMinutes;
    }

    public String getPatientId() { return patientId; }
    public int getPosition() { return position; }
    public int getPatientsAhead() { return patientsAhead; }
    public long getEstimatedMinutes() { return estimatedMinutes; }
}
