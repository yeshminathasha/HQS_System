package util;

import model.Patient;

public class ConsoleHelper {

    public static void printMenu() {
        System.out.println("\n--- Smart Hospital Queue System ---");
        System.out.println("1. Register New Patient");
        System.out.println("2. Update Patient Details");
        System.out.println("3. Cancel Appointment");
        System.out.println("4. Search Patient by ID");
        System.out.println("5. Search Patient by Name");
        System.out.println("6. Display Live Queue");
        System.out.println("7. View Estimated Waiting Time");
        System.out.println("8. Recommend Doctor");
        System.out.println("9. View Appointment History");
        System.out.println("0. Exit");
        System.out.print("Enter choice: ");
    }

    public static void printPatientTable(Patient[] patients) {
        if (patients == null || patients.length == 0) {
            System.out.println("No patients found.");
            return;
        }
        
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        System.out.printf("| %-10s | %-15s | %-12s | %-12s | %-15s | %-10s | %-10s | %-8s |\n", 
                          "ID", "Name", "Contact", "Department", "Doctor", "Date", "Emergency", "Priority");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        
        for (Patient p : patients) {
            if (p != null) {
                System.out.printf("| %-10s | %-15s | %-12s | %-12s | %-15s | %-10s | %-10s | %-8d |\n",
                        p.getPatientId(),
                        truncate(p.getName(), 15),
                        truncate(p.getContactNumber(), 12),
                        truncate(p.getDepartment(), 12),
                        truncate(p.getDoctorName(), 15),
                        p.getAppointmentDate(),
                        p.isEmergency() ? "Yes" : "No",
                        p.getPriorityLevel());
            }
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
    }

    public static void printSinglePatient(Patient p) {
        if (p == null) {
            System.out.println("Patient not found.");
            return;
        }
        System.out.println("\n--- Patient Details ---");
        System.out.println("ID: " + p.getPatientId());
        System.out.println("Name: " + p.getName());
        System.out.println("Contact: " + p.getContactNumber());
        System.out.println("Department: " + p.getDepartment());
        System.out.println("Doctor: " + p.getDoctorName());
        System.out.println("Date: " + p.getAppointmentDate());
        System.out.println("Time: " + p.getAppointmentTime());
        System.out.println("Emergency: " + (p.isEmergency() ? "Yes" : "No"));
        System.out.println("Priority Level: " + p.getPriorityLevel());
        System.out.println("Status: " + p.getStatus());
    }

    private static String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }
}
