package com.smarthospital.mapper;

import com.smarthospital.dto.PatientRequest;
import com.smarthospital.dto.PatientResponse;
import com.smarthospital.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patientId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "registeredAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "waitMinutes", ignore = true)
    Patient toEntity(PatientRequest request);

    PatientResponse toResponse(Patient patient);
}
