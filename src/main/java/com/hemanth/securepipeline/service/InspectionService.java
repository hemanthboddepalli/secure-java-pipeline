package com.hemanth.securepipeline.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import com.hemanth.securepipeline.config.InspectionPolicy;
import com.hemanth.securepipeline.domain.Disposition;
import com.hemanth.securepipeline.domain.Inspection;

@Service
public class InspectionService {
    private static final String SUMMARY_TEMPLATE =
            "Wafer ${waferId}: ${defectCount} defects, disposition ${disposition}";

    private final InspectionPolicy policy;
    private final Clock clock;
    private final Map<UUID, Inspection> inspections = new ConcurrentHashMap<>();

    public InspectionService(InspectionPolicy policy, Clock clock) {
        this.policy = policy;
        this.clock = clock;
    }

    public Inspection create(String waferId, int defectCount) {
        if (waferId == null || waferId.isBlank()) {
            throw new IllegalArgumentException("waferId must not be blank");
        }
        Disposition disposition = policy.classify(defectCount);
        UUID id = UUID.randomUUID();
        String summary = new StringSubstitutor(Map.of(
                "waferId", waferId,
                "defectCount", Integer.toString(defectCount),
                "disposition", disposition.name()))
                .replace(SUMMARY_TEMPLATE);
        Inspection inspection = new Inspection(id, waferId, defectCount, disposition, summary, Instant.now(clock));
        inspections.put(id, inspection);
        return inspection;
    }

    public Inspection get(UUID id) {
        Inspection inspection = inspections.get(id);
        if (inspection == null) {
            throw new InspectionNotFoundException(id);
        }
        return inspection;
    }

    public List<Inspection> list() {
        return inspections.values().stream()
                .sorted(Comparator.comparing(Inspection::createdAt).thenComparing(Inspection::id))
                .toList();
    }
}

