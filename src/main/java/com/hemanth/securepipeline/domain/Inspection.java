package com.hemanth.securepipeline.domain;

import java.time.Instant;
import java.util.UUID;

public record Inspection(
        UUID id,
        String waferId,
        int defectCount,
        Disposition disposition,
        String summary,
        Instant createdAt) {
}

