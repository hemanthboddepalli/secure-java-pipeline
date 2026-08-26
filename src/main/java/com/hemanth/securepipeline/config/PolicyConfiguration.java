package com.hemanth.securepipeline.config;

import org.apache.commons.configuration2.BaseConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PolicyConfiguration {

    @Bean
    InspectionPolicy inspectionPolicy(
            @Value("${inspection.thresholds.pass-max:5}") int passMax,
            @Value("${inspection.thresholds.review-max:20}") int reviewMax) {
        BaseConfiguration configuration = new BaseConfiguration();
        configuration.addProperty("inspection.thresholds.pass-max", passMax);
        configuration.addProperty("inspection.thresholds.review-max", reviewMax);
        return InspectionPolicy.from(configuration);
    }
}

