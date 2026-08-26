package com.hemanth.securepipeline.api;

import java.time.Instant;
import java.util.UUID;

import com.hemanth.securepipeline.domain.Disposition;
import com.hemanth.securepipeline.domain.Inspection;

public record InspectionResponse(
        UUID id,
        String waferId,
        int defectCount,
        Disposition disposition,
        String summary,
        Instant createdAt) {

    static InspectionResponse from(Inspection inspection) {
        return new InspectionResponse(
                inspection.id(), inspection.waferId(), inspection.defectCount(),
                inspection.disposition(), inspection.summary(), inspection.createdAt());
    }
}

