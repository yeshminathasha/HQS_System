package com.smarthospital.service;

import com.smarthospital.dto.DailyReport;
import com.smarthospital.entity.PatientStatus;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.FacetOperation;
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

        MatchOperation dayMatch = Aggregation.match(day);
        FacetOperation facet = Aggregation.facet()
                .and(Aggregation.group("status").count().as("count")).as("byStatus")
                .and(Aggregation.group("department").count().as("count")).as("byDepartment")
                .and(Aggregation.group("doctorName").count().as("count")).as("byDoctor")
                .and(Aggregation.match(Criteria.where("status").is(PatientStatus.COMPLETED).and("waitMinutes").gt(0)),
                        Aggregation.group().avg("waitMinutes").as("avg")).as("avgWait");

        AggregationResults<Map> results = mongoTemplate.aggregate(
                Aggregation.newAggregation(dayMatch, facet), "patients", Map.class);

        Map<String, Long> byStatus = countFacet("byStatus", results);
        Map<String, Long> byDepartment = countFacet("byDepartment", results);
        Map<String, Long> byDoctor = countFacet("byDoctor", results);
        Long avgWait = averageFacet(results);

        DailyReport report = new DailyReport();
        report.setDate(date);
        report.setWaiting(byStatus.getOrDefault(PatientStatus.WAITING.name(), 0L));
        report.setInConsultation(byStatus.getOrDefault(PatientStatus.IN_CONSULTATION.name(), 0L));
        report.setCompleted(byStatus.getOrDefault(PatientStatus.COMPLETED.name(), 0L));
        report.setCancelled(byStatus.getOrDefault(PatientStatus.CANCELLED.name(), 0L));
        report.setTotalRegistered(byStatus.values().stream().mapToLong(Long::longValue).sum());
        report.setAvgWaitMinutes(avgWait);

        byDepartment.forEach((name, count) -> report.getByDepartment().add(new DailyReport.Stat(name, count)));
        byDoctor.forEach((name, count) -> report.getByDoctor().add(new DailyReport.Stat(name, count)));
        return report;
    }

    private Map<String, Long> countFacet(String facetName, AggregationResults<Map> results) {
        Map<String, Long> counts = new LinkedHashMap<>();
        List<Map> rows = facetRows(facetName, results);
        for (Map row : rows) {
            Object id = row.get("_id");
            Object count = row.get("count");
            if (id != null) {
                counts.put(id.toString(), count instanceof Number n ? n.longValue() : 0L);
            }
        }
        return counts;
    }

    private Long averageFacet(AggregationResults<Map> results) {
        List<Map> rows = facetRows("avgWait", results);
        if (!rows.isEmpty()) {
            Object avg = rows.get(0).get("avg");
            if (avg instanceof Number n) {
                return n.longValue();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Map> facetRows(String facetName, AggregationResults<Map> results) {
        Object facet = results.getMappedResults().stream().findFirst()
                .map(map -> map.get(facetName))
                .orElse(null);
        return facet instanceof List<?> list ? (List<Map>) list : List.of();
    }
}
