package main;

import model.Patient;
import service.HospitalService;
import util.ConsoleHelper;
import util.InputValidator;

import java.time.LocalDate;
import java.util.Scanner;

public class HospitalManagementSystem {
    
    public static void main(String[] args) {
        HospitalService service = new HospitalService();
        Scanner sc = new Scanner(System.in);
        
        // Dummy data uses the proper constructor
        String today = LocalDate.now().toString();
        service.registerPatient(new Patient("P001", "John Doe", "1234567890", "Cardiology", "Dr. Smith", today, "10:00", false, 0, "Waiting"));
        service.registerPatient(new Patient("P002", "Jane Smith", "0987654321", "Neurology", "Dr. Jones", today, "10:30", true, 1, "Waiting"));
        service.registerPatient(new Patient("P003", "Alice Brown", "1112223333", "General", "Dr. Adams", today, "11:00", false, 0, "Waiting"));

        System.out.println("Welcome to Smart Hospital Patient Queue System!");
        
        boolean running = true;
        while (running) {
            ConsoleHelper.printMenu();
            int choice = InputValidator.getInt(sc, "Choice: ");
            
            switch (choice) {
                case 1:
                    registerPatient(service, sc);
                    break;
                case 2:
                    updatePatient(service, sc);
                    break;
                case 3:
                    cancelAppointment(service, sc);
                    break;
                case 4:
                    callNext(service, sc);
                    break;
                case 5:
                    completeConsultation(service, sc);
                    break;
                case 6:
                    searchById(service, sc);
                    break;
                case 7:
                    searchByName(service, sc);
                    break;
                case 8:
                    displayQueue(service);
                    break;
                case 9:
                    viewWaitTime(service, sc);
                    break;
                case 10:
                    recommendDoctor(service);
                    break;
                case 11:
                    displayHistory(service);
                    break;
                case 0:
                    running = false;
                    System.out.println("Exiting System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number between 0 and 11.");
            }
        }
        sc.close();
    }

    private static void registerPatient(HospitalService service, Scanner sc) {
        String id = InputValidator.getString(sc, "Enter Patient ID: ");
        if (service.searchById(id) != null) {
            System.out.println("Error: Patient ID already exists.");
            return;
        }
        
        String name = InputValidator.getString(sc, "Enter Name: ");
        String contact = InputValidator.getString(sc, "Enter Contact Number: ");
        String dept = InputValidator.getString(sc, "Enter Department: ");
        String doctor = InputValidator.getString(sc, "Enter Doctor Name: ");
        String date = InputValidator.getString(sc, "Enter Date (YYYY-MM-DD): ");
        String time = InputValidator.getString(sc, "Enter Time (HH:MM): ");
        boolean emergency = InputValidator.getBoolean(sc, "Is Emergency?");
        int priority = 0;
        if (emergency) {
            priority = InputValidator.getIntInRange(sc, "Enter Priority Level (1-High, 2-Medium, 3-Low): ", 1, 3);
        }

        Patient p = new Patient(id, name, contact, dept, doctor, date, time, emergency, priority, "Waiting");
        service.registerPatient(p);
        System.out.println("Patient registered successfully!");
    }

    private static void updatePatient(HospitalService service, Scanner sc) {
        String id = InputValidator.getString(sc, "Enter Patient ID to Update: ");
        Patient existing = service.searchById(id);
        if (existing == null) {
            System.out.println("Patient not found.");
            return;
        }

        System.out.println("Enter New Details:");
        String name = InputValidator.getString(sc, "Enter Name (" + existing.getName() + "): ");
        String contact = InputValidator.getString(sc, "Enter Contact Number (" + existing.getContactNumber() + "): ");
        String dept = InputValidator.getString(sc, "Enter Department (" + existing.getDepartment() + "): ");
        String doctor = InputValidator.getString(sc, "Enter Doctor Name (" + existing.getDoctorName() + "): ");
        String date = InputValidator.getString(sc, "Enter Date (" + existing.getAppointmentDate() + "): ");
        String time = InputValidator.getString(sc, "Enter Time (" + existing.getAppointmentTime() + "): ");
        boolean emergency = InputValidator.getBoolean(sc, "Is Emergency? (" + existing.isEmergency() + ")");
        int priority = 0;
        if (emergency) {
            priority = InputValidator.getIntInRange(sc, "Enter Priority Level: ", 1, 3);
        }

        Patient updated = new Patient(id, name, contact, dept, doctor, date, time, emergency, priority, "Waiting");
        if (service.updatePatient(id, updated)) {
            System.out.println("Patient updated successfully!");
        } else {
            System.out.println("Failed to update patient.");
        }
    }

    private static void cancelAppointment(HospitalService service, Scanner sc) {
        String id = InputValidator.getString(sc, "Enter Patient ID to Cancel: ");
        Patient cancelled = service.cancelAppointment(id);
        if (cancelled != null) {
            System.out.println("Appointment cancelled for: " + cancelled.getName());
        } else {
            System.out.println("Patient not found in active queue.");
        }
    }

    private static void callNext(HospitalService service, Scanner sc) {
        String id = InputValidator.getString(sc, "Enter Patient ID to Call: ");
        Patient patient = service.callNext(id);
        if (patient != null) {
            System.out.println("Called next: " + patient.getName() + " (" + patient.getPatientId() + ")");
        } else {
            System.out.println("Patient not found in active queue.");
        }
    }

    private static void completeConsultation(HospitalService service, Scanner sc) {
        String id = InputValidator.getString(sc, "Enter Patient ID to Complete: ");
        Patient patient = service.completeConsultation(id);
        if (patient != null) {
            System.out.println("Consultation completed for: " + patient.getName());
        } else {
            System.out.println("Patient not found or not in consultation.");
        }
    }

    private static void searchById(HospitalService service, Scanner sc) {
        String id = InputValidator.getString(sc, "Enter Patient ID: ");
        Patient p = service.searchById(id);
        ConsoleHelper.printSinglePatient(p);
    }

    private static void searchByName(HospitalService service, Scanner sc) {
        String name = InputValidator.getString(sc, "Enter Patient Name: ");
        Patient[] patients = service.searchByName(name);
        System.out.println("\n--- Search Results ---");
        ConsoleHelper.printPatientTable(patients);
    }

    private static void displayQueue(HospitalService service) {
        System.out.println("\n--- Live Patient Queue ---");
        ConsoleHelper.printPatientTable(service.displayQueue());
    }

    private static void viewWaitTime(HospitalService service, Scanner sc) {
        String id = InputValidator.getString(sc, "Enter Patient ID: ");
        int time = service.getEstimatedWaitingTime(id);
        if (time == -1) {
            System.out.println("Patient not found in the queue.");
        } else {
            System.out.println("Estimated waiting time for Patient " + id + " is " + time + " minutes.");
        }
    }

    private static void recommendDoctor(HospitalService service) {
        System.out.println("\nRecommended Doctor based on shortest queue:");
        System.out.println(service.recommendDoctor());
    }

    private static void displayHistory(HospitalService service) {
        System.out.println("\n--- Appointment History ---");
        ConsoleHelper.printPatientTable(service.displayHistory());
    }
}
