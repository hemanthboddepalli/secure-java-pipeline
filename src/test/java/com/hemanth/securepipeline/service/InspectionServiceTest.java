package com.hemanth.securepipeline.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hemanth.securepipeline.config.InspectionPolicy;
import com.hemanth.securepipeline.domain.Disposition;
import com.hemanth.securepipeline.domain.Inspection;

class InspectionServiceTest {
    private InspectionService service;

    @BeforeEach
    void setUp() {
        InspectionPolicy policy = InspectionPolicy.from(new MapConfiguration(
                Map.of("inspection.thresholds.pass-max", 5, "inspection.thresholds.review-max", 20)));
        service = new InspectionService(policy, Clock.fixed(Instant.parse("2026-08-26T00:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void createsAndRetrievesInspectionWithSafeSummary() {
        Inspection inspection = service.create("WAFER-42", 7);

        assertThat(inspection.disposition()).isEqualTo(Disposition.REVIEW);
        assertThat(inspection.summary()).isEqualTo("Wafer WAFER-42: 7 defects, disposition REVIEW");
        assertThat(inspection.createdAt()).isEqualTo(Instant.parse("2026-08-26T00:00:00Z"));
        assertThat(service.get(inspection.id())).isEqualTo(inspection);
        assertThat(service.list()).containsExactly(inspection);
    }

    @Test
    void rejectsBlankWaferAndReportsUnknownInspection() {
        assertThatThrownBy(() -> service.create(" ", 0)).isInstanceOf(IllegalArgumentException.class);
        UUID missingId = UUID.randomUUID();
        assertThatThrownBy(() -> service.get(missingId))
                .isInstanceOf(InspectionNotFoundException.class)
                .hasMessageContaining(missingId.toString());
    }
}

