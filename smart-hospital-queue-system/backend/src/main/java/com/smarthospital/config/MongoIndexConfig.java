package com.smarthospital.config;

import com.smarthospital.entity.Appointment;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexFilter;
import org.springframework.stereotype.Component;

@Component
public class MongoIndexConfig implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MongoIndexConfig.class);

    private final MongoTemplate mongoTemplate;

    public MongoIndexConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            mongoTemplate.indexOps(Appointment.class).ensureIndex(
                    new Index("doctorName", Sort.Direction.ASC)
                            .on("appointmentDate", Sort.Direction.ASC)
                            .on("appointmentTime", Sort.Direction.ASC)
                            .named("uq_scheduled_slot")
                            .unique()
                            .partial(scheduledOnly()));
        } catch (Exception ex) {
            log.warn("Could not create unique slot index (duplicate SCHEDULED data may exist); "
                    + "deduplicate 'appointments' and restart: {}", ex.getMessage());
        }
    }

    private IndexFilter scheduledOnly() {
        return () -> Document.parse("{\"status\": \"SCHEDULED\"}");
    }
}
