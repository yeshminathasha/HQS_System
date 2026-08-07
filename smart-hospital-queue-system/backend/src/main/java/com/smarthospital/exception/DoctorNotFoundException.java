package com.smarthospital.exception;

public class DoctorNotFoundException extends RuntimeException {
    public DoctorNotFoundException(String doctorName) {
        super("Doctor not found with name: " + doctorName);
    }
}
