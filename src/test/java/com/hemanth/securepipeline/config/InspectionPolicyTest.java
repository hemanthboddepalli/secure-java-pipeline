package com.hemanth.securepipeline.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Test;

import com.hemanth.securepipeline.domain.Disposition;

class InspectionPolicyTest {

    @Test
    void classifiesBoundaryValues() {
        InspectionPolicy policy = InspectionPolicy.from(new MapConfiguration(
                java.util.Map.of("inspection.thresholds.pass-max", 5, "inspection.thresholds.review-max", 20)));

        assertThat(policy.classify(0)).isEqualTo(Disposition.PASS);
        assertThat(policy.classify(5)).isEqualTo(Disposition.PASS);
        assertThat(policy.classify(6)).isEqualTo(Disposition.REVIEW);
        assertThat(policy.classify(20)).isEqualTo(Disposition.REVIEW);
        assertThat(policy.classify(21)).isEqualTo(Disposition.REJECT);
    }

    @Test
    void rejectsInvalidConfigurationAndNegativeCounts() {
        assertThatThrownBy(() -> InspectionPolicy.from(new MapConfiguration(
                java.util.Map.of("inspection.thresholds.pass-max", 20, "inspection.thresholds.review-max", 5))))
                .isInstanceOf(IllegalArgumentException.class);

        InspectionPolicy policy = InspectionPolicy.from(new MapConfiguration(
                java.util.Map.of("inspection.thresholds.pass-max", 5, "inspection.thresholds.review-max", 20)));
        assertThatThrownBy(() -> policy.classify(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}

