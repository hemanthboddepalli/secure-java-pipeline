package com.hemanth.securepipeline.config;

import org.apache.commons.configuration2.Configuration;

import com.hemanth.securepipeline.domain.Disposition;

public final class InspectionPolicy {
    private final int passMax;
    private final int reviewMax;

    private InspectionPolicy(int passMax, int reviewMax) {
        if (passMax < 0 || reviewMax <= passMax) {
            throw new IllegalArgumentException("Thresholds must satisfy 0 <= passMax < reviewMax");
        }
        this.passMax = passMax;
        this.reviewMax = reviewMax;
    }

    public static InspectionPolicy from(Configuration configuration) {
        return new InspectionPolicy(
                configuration.getInt("inspection.thresholds.pass-max"),
                configuration.getInt("inspection.thresholds.review-max"));
    }

    public Disposition classify(int defectCount) {
        if (defectCount < 0) {
            throw new IllegalArgumentException("defectCount must be nonnegative");
        }
        if (defectCount <= passMax) {
            return Disposition.PASS;
        }
        if (defectCount <= reviewMax) {
            return Disposition.REVIEW;
        }
        return Disposition.REJECT;
    }
}

