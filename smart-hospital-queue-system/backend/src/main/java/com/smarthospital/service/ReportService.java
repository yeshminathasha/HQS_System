package com.smarthospital.service;

import com.smarthospital.dto.DailyReport;
import com.smarthospital.entity.PatientStatus;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final MongoTemplate mongoTemplate;

    public ReportService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public DailyReport getDailyReport(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        Criteria day = Criteria.where("registeredAt").gte(start).lt(end);

        Map<String, Long> byStatus = countBy(day, "status");
        Long avgWait = averageWait(start, end);

        DailyReport report = new DailyReport();
        report.setDate(date);
        report.setWaiting(byStatus.getOrDefault(PatientStatus.WAITING.name(), 0L));
        report.setInConsultation(byStatus.getOrDefault(PatientStatus.IN_CONSULTATION.name(), 0L));
        report.setCompleted(byStatus.getOrDefault(PatientStatus.COMPLETED.name(), 0L));
        report.setCancelled(byStatus.getOrDefault(PatientStatus.CANCELLED.name(), 0L));
        report.setTotalRegistered(byStatus.values().stream().mapToLong(Long::longValue).sum());
        report.setAvgWaitMinutes(avgWait);

        countBy(day, "department").forEach((name, count) -> report.getByDepartment().add(new DailyReport.Stat(name, count)));
        countBy(day, "doctorName").forEach((name, count) -> report.getByDoctor().add(new DailyReport.Stat(name, count)));
        return report;
    }

    private Map<String, Long> countBy(Criteria day, String groupField) {
        MatchOperation match = Aggregation.match(day);
        GroupOperation group = Aggregation.group(groupField).count().as("count");
        AggregationResults<Map> results = mongoTemplate.aggregate(
                Aggregation.newAggregation(match, group), "patients", Map.class);

        Map<String, Long> counts = new LinkedHashMap<>();
        for (Map doc : results.getMappedResults()) {
            Object id = doc.get("_id");
            if (id != null) {
                Object count = doc.get("count");
                counts.put(id.toString(), count instanceof Number n ? n.longValue() : 0L);
            }
        }
        return counts;
    }

    private Long averageWait(LocalDateTime start, LocalDateTime end) {
        MatchOperation match = Aggregation.match(
                Criteria.where("registeredAt").gte(start).lt(end)
                        .and("status").is(PatientStatus.COMPLETED)
                        .and("waitMinutes").gt(0));
        GroupOperation group = Aggregation.group().avg("waitMinutes").as("avg");
        AggregationResults<Map> results = mongoTemplate.aggregate(
                Aggregation.newAggregation(match, group), "patients", Map.class);
        List<Map> mapped = results.getMappedResults();
        if (mapped.isEmpty()) {
            return null;
        }
        Object avg = mapped.get(0).get("avg");
        return avg instanceof Number n ? n.longValue() : null;
    }
}
