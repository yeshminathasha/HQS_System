package com.smarthospital.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DailyReport {

    private LocalDate date;
    private long totalRegistered;
    private long waiting;
    private long inConsultation;
    private long completed;
    private long cancelled;
    private Long avgWaitMinutes;
    private List<Stat> byDepartment = new ArrayList<>();
    private List<Stat> byDoctor = new ArrayList<>();

    public record Stat(String name, long count) {}

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public long getTotalRegistered() { return totalRegistered; }
    public void setTotalRegistered(long totalRegistered) { this.totalRegistered = totalRegistered; }

    public long getWaiting() { return waiting; }
    public void setWaiting(long waiting) { this.waiting = waiting; }

    public long getInConsultation() { return inConsultation; }
    public void setInConsultation(long inConsultation) { this.inConsultation = inConsultation; }

    public long getCompleted() { return completed; }
    public void setCompleted(long completed) { this.completed = completed; }

    public long getCancelled() { return cancelled; }
    public void setCancelled(long cancelled) { this.cancelled = cancelled; }

    public Long getAvgWaitMinutes() { return avgWaitMinutes; }
    public void setAvgWaitMinutes(Long avgWaitMinutes) { this.avgWaitMinutes = avgWaitMinutes; }

    public List<Stat> getByDepartment() { return byDepartment; }
    public void setByDepartment(List<Stat> byDepartment) { this.byDepartment = byDepartment; }

    public List<Stat> getByDoctor() { return byDoctor; }
    public void setByDoctor(List<Stat> byDoctor) { this.byDoctor = byDoctor; }
}
