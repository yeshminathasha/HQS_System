package com.smarthospital.controller;

import com.smarthospital.dto.PatientResponse;
import com.smarthospital.entity.PatientStatus;
import com.smarthospital.exception.PatientNotFoundException;
import com.smarthospital.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @Test
    void registerValidPatientReturns201() throws Exception {
        PatientResponse response = response("P001");
        when(patientService.registerPatient(any())).thenReturn(response);

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John Doe\",\"contactNumber\":\"0771234567\",\"department\":\"General\",\"doctorName\":\"Dr. Adams\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value("P001"));
    }

    @Test
    void registerInvalidPatientReturns400() throws Exception {
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"contactNumber\":\"bad\",\"department\":\"General\",\"doctorName\":\"Dr. Adams\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Name is required")));
    }

    @Test
    void getMissingPatientReturns404Json() throws Exception {
        when(patientService.getPatientById("P999")).thenThrow(new PatientNotFoundException("P999"));

        mockMvc.perform(get("/api/patients/P999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("P999")));
    }

    @Test
    void updateStatusReturns200() throws Exception {
        PatientResponse response = response("P001");
        response.setStatus(PatientStatus.IN_CONSULTATION);
        when(patientService.updateStatus(eq("P001"), eq(PatientStatus.IN_CONSULTATION))).thenReturn(response);

        mockMvc.perform(patch("/api/patients/P001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_CONSULTATION\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_CONSULTATION"));
    }

    @Test
    void updateStatusWithInvalidEnumReturns400() throws Exception {
        mockMvc.perform(patch("/api/patients/P001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BOGUS\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletePatientReturns204() throws Exception {
        mockMvc.perform(delete("/api/patients/P001"))
                .andExpect(status().isNoContent());
    }

    private PatientResponse response(String id) {
        PatientResponse r = new PatientResponse();
        r.setId("abc");
        r.setPatientId(id);
        r.setName("John Doe");
        r.setContactNumber("0771234567");
        r.setDepartment("General");
        r.setDoctorName("Dr. Adams");
        r.setEmergency(false);
        r.setPriorityLevel(0);
        r.setStatus(PatientStatus.WAITING);
        return r;
    }
}
