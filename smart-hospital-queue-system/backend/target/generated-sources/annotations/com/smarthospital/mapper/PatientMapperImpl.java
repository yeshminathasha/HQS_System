package com.smarthospital.mapper;

import com.smarthospital.dto.PatientRequest;
import com.smarthospital.dto.PatientResponse;
import com.smarthospital.entity.Patient;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-02T00:07:34+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class PatientMapperImpl implements PatientMapper {

    @Override
    public Patient toEntity(PatientRequest request) {
        if ( request == null ) {
            return null;
        }

        Patient patient = new Patient();

        patient.setName( request.getName() );
        patient.setContactNumber( request.getContactNumber() );
        patient.setDepartment( request.getDepartment() );
        patient.setDoctorName( request.getDoctorName() );
        patient.setEmergency( request.isEmergency() );
        if ( request.getPriorityLevel() != null ) {
            patient.setPriorityLevel( request.getPriorityLevel() );
        }

        return patient;
    }

    @Override
    public PatientResponse toResponse(Patient patient) {
        if ( patient == null ) {
            return null;
        }

        PatientResponse patientResponse = new PatientResponse();

        patientResponse.setId( patient.getId() );
        patientResponse.setPatientId( patient.getPatientId() );
        patientResponse.setName( patient.getName() );
        patientResponse.setContactNumber( patient.getContactNumber() );
        patientResponse.setDepartment( patient.getDepartment() );
        patientResponse.setDoctorName( patient.getDoctorName() );
        patientResponse.setEmergency( patient.isEmergency() );
        patientResponse.setPriorityLevel( patient.getPriorityLevel() );
        patientResponse.setStatus( patient.getStatus() );
        patientResponse.setRegisteredAt( patient.getRegisteredAt() );
        patientResponse.setCompletedAt( patient.getCompletedAt() );
        patientResponse.setWaitMinutes( patient.getWaitMinutes() );

        return patientResponse;
    }
}
