package com.smarthospital.dto;

import java.util.List;

public class DoctorResponse {

    private String id;
    private String name;
    private String department;
    private List<String> workDays;
    private String startTime;
    private String endTime;
    private long activeQueueCount;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public List<String> getWorkDays() { return workDays; }
    public void setWorkDays(List<String> workDays) { this.workDays = workDays; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public long getActiveQueueCount() { return activeQueueCount; }
    public void setActiveQueueCount(long activeQueueCount) { this.activeQueueCount = activeQueueCount; }
}
