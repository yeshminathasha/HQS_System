package com.smarthospital.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "doctors")
public class Doctor {

    @Id
    private String id;
    @Indexed(unique = true)
    private String name;
    private String department;
    private List<String> workDays; // e.g. ["MON", "TUE", "WED"]
    private String startTime; // "09:00"
    private String endTime;   // "17:00"

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
}
