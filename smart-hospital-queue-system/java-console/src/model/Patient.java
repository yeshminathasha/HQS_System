package model;

/**
 * Pure Data Model representing a Patient in the Hospital Queue System.
 * No business logic, purely getters, setters, and state.
 */
public class Patient {
    private String patientId;
    private String name;
    private String contactNumber;
    private String department;
    private String doctorName;
    private String appointmentDate;
    private String appointmentTime;
    private boolean emergency;
    private int priorityLevel; // 1-5, 0 if not emergency
    private String status; // Waiting, Completed, Cancelled

    public Patient(String patientId, String name, String contactNumber, String department, 
                   String doctorName, String appointmentDate, String appointmentTime, 
                   boolean emergency, int priorityLevel, String status) {
        this.patientId = patientId;
        this.name = name;
        this.contactNumber = contactNumber;
        this.department = department;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.emergency = emergency;
        // If not emergency, priority is 0 as per requirements
        this.priorityLevel = emergency ? priorityLevel : 0;
        this.status = status;
    }

    /**
     * Copy constructor. Used to snapshot patients into the appointment history
     * so later updates to the live queue do not mutate historical records.
     */
    public Patient(Patient other) {
        this(other.patientId, other.name, other.contactNumber, other.department, other.doctorName,
             other.appointmentDate, other.appointmentTime, other.emergency, other.priorityLevel, other.status);
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public boolean isEmergency() {
        return emergency;
    }

    public void setEmergency(boolean emergency) {
        this.emergency = emergency;
        if (!this.emergency) {
            this.priorityLevel = 0;
        }
    }

    public int getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(int priorityLevel) {
        if (this.emergency) {
            this.priorityLevel = priorityLevel;
        } else {
            this.priorityLevel = 0;
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("%-10s | %-20s | %-15s | %-15s | %-15s | %-12s | %-10s | %-9s | %-8d | %-10s",
                patientId, name, contactNumber, doctorName, department,
                appointmentDate, appointmentTime, (emergency ? "Yes" : "No"), priorityLevel, status);
    }
}
