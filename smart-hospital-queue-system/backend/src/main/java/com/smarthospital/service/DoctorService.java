package com.smarthospital.service;

import com.smarthospital.dto.DoctorResponse;
import com.smarthospital.dto.RecommendResponse;
import com.smarthospital.entity.Doctor;
import com.smarthospital.entity.PatientStatus;
import com.smarthospital.repository.DoctorRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.bson.Document;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final MongoTemplate mongoTemplate;

    public DoctorService(DoctorRepository doctorRepository, MongoTemplate mongoTemplate) {
        this.doctorRepository = doctorRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<DoctorResponse> getDoctorsWithWorkload() {
        Map<String, Long> workload = getActiveQueueCounts();
        return doctorRepository.findAll().stream()
                .map(doctor -> toResponse(doctor, workload.getOrDefault(doctor.getName(), 0L)))
                .toList();
    }

    public RecommendResponse recommendDoctor() {
        Map<String, Long> workload = getActiveQueueCounts();
        Doctor best = null;
        long min = Long.MAX_VALUE;
        for (Doctor doctor : doctorRepository.findAll()) {
            long count = workload.getOrDefault(doctor.getName(), 0L);
            if (count < min) {
                min = count;
                best = doctor;
            }
        }
        if (best == null) {
            return new RecommendResponse("No doctors registered", "", 0);
        }
        return new RecommendResponse(best.getName(), best.getDepartment(), min);
    }

    Map<String, Long> getActiveQueueCounts() {
        MatchOperation match = Aggregation.match(
                Criteria.where("status").in(PatientStatus.WAITING, PatientStatus.IN_CONSULTATION)
                        .and("doctorName").exists(true).ne(null));
        GroupOperation group = Aggregation.group("doctorName").count().as("count");
        AggregationResults<Document> results = mongoTemplate.aggregate(
                Aggregation.newAggregation(match, group), "patients", Document.class);

        Map<String, Long> counts = new LinkedHashMap<>();
        for (Document doc : results.getMappedResults()) {
            Object id = doc.get("_id");
            if (id != null) {
                Object count = doc.get("count");
                counts.put(id.toString(), count instanceof Number n ? n.longValue() : 0L);
            }
        }
        return counts;
    }

    private DoctorResponse toResponse(Doctor doctor, long queueCount) {
        DoctorResponse response = new DoctorResponse();
        response.setId(doctor.getId());
        response.setName(doctor.getName());
        response.setDepartment(doctor.getDepartment());
        response.setWorkDays(doctor.getWorkDays());
        response.setStartTime(doctor.getStartTime());
        response.setEndTime(doctor.getEndTime());
        response.setActiveQueueCount(queueCount);
        return response;
    }
}
