package service;

import datastructure.AppointmentHistoryDLL;
import datastructure.PatientQueueDLL;
import model.Patient;

public class HospitalService {
    private PatientQueueDLL activeQueue;
    private AppointmentHistoryDLL historyList;

    public HospitalService() {
        this.activeQueue = new PatientQueueDLL();
        this.historyList = new AppointmentHistoryDLL();
    }

    public void registerPatient(Patient patient) {
        activeQueue.insertPatient(patient);
        // Snapshot into history so later queue updates do not mutate historical records
        historyList.addHistory(new Patient(patient));
    }

    public boolean updatePatient(String patientId, Patient updatedData) {
        Patient existing = activeQueue.findById(patientId);
        if (existing == null) {
            return false;
        }
        
        // Remove and re-insert if priority or emergency status changes (to maintain queue order)
        if (existing.isEmergency() != updatedData.isEmergency() || 
            existing.getPriorityLevel() != updatedData.getPriorityLevel()) {
            
            activeQueue.deletePatient(patientId);
            
            existing.setName(updatedData.getName());
            existing.setContactNumber(updatedData.getContactNumber());
            existing.setDepartment(updatedData.getDepartment());
            existing.setDoctorName(updatedData.getDoctorName());
            existing.setAppointmentDate(updatedData.getAppointmentDate());
            existing.setAppointmentTime(updatedData.getAppointmentTime());
            existing.setEmergency(updatedData.isEmergency());
            existing.setPriorityLevel(updatedData.getPriorityLevel());
            
            activeQueue.insertPatient(existing);
        } else {
            // Just update basic fields
            existing.setName(updatedData.getName());
            existing.setContactNumber(updatedData.getContactNumber());
            existing.setDepartment(updatedData.getDepartment());
            existing.setDoctorName(updatedData.getDoctorName());
            existing.setAppointmentDate(updatedData.getAppointmentDate());
            existing.setAppointmentTime(updatedData.getAppointmentTime());
        }
        return true;
    }

    public Patient cancelAppointment(String patientId) {
        Patient patient = activeQueue.findById(patientId);
        if (patient == null) {
            return null;
        }
        patient.setStatus("Cancelled");
        activeQueue.deletePatient(patientId);
        // Keep the history snapshot in sync with the cancellation
        Patient historyEntry = historyList.searchById(patientId);
        if (historyEntry != null) {
            historyEntry.setStatus("Cancelled");
        }
        return patient;
    }

    public Patient callNext(String patientId) {
        Patient patient = activeQueue.findById(patientId);
        if (patient == null) {
            return null;
        }
        patient.setStatus("In Consultation");
        Patient historyEntry = historyList.searchById(patientId);
        if (historyEntry != null) {
            historyEntry.setStatus("In Consultation");
        }
        return patient;
    }

    public Patient completeConsultation(String patientId) {
        Patient patient = activeQueue.findById(patientId);
        if (patient == null) {
            return null;
        }
        if (!"In Consultation".equalsIgnoreCase(patient.getStatus())) {
            return null;
        }
        patient.setStatus("Completed");
        activeQueue.deletePatient(patientId);
        Patient historyEntry = historyList.searchById(patientId);
        if (historyEntry != null) {
            historyEntry.setStatus("Completed");
        }
        return patient;
    }

    public Patient searchById(String patientId) {
        return activeQueue.findById(patientId);
    }

    public Patient[] searchByName(String name) {
        return activeQueue.findByName(name);
    }

    public Patient[] displayQueue() {
        return activeQueue.getAllPatients();
    }

    public int getEstimatedWaitingTime(String patientId) {
        int position = activeQueue.getPosition(patientId);
        if (position == -1) {
            return -1; // Not found in active queue
        }
        // Assume 15 minutes per patient ahead in the queue
        return position * 15;
    }

    public String recommendDoctor() {
        if (activeQueue.isEmpty()) {
            return "No recommendations (Queue is empty)";
        }
        
        // Array of predefined doctors for the coursework
        String[] doctors = {"Dr. Smith", "Dr. Jones", "Dr. Adams"};
        int[] counts = new int[doctors.length];
        
        Patient[] allPatients = activeQueue.getAllPatients();
        for (Patient p : allPatients) {
            for (int i = 0; i < doctors.length; i++) {
                if (p.getDoctorName().equalsIgnoreCase(doctors[i])) {
                    counts[i]++;
                    break;
                }
            }
        }
        
        int minCount = counts[0];
        String bestDoctor = doctors[0];
        for (int i = 1; i < doctors.length; i++) {
            if (counts[i] < minCount) {
                minCount = counts[i];
                bestDoctor = doctors[i];
            }
        }
        
        return bestDoctor + " (Queue Size: " + minCount + ")";
    }

    public Patient[] displayHistory() {
        return historyList.getAllHistoryForward();
    }
}
