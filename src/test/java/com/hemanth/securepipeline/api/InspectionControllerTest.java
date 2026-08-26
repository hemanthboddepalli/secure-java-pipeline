package com.hemanth.securepipeline.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.hemanth.securepipeline.config.InspectionPolicy;
import com.hemanth.securepipeline.service.InspectionService;

class InspectionControllerTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        InspectionPolicy policy = InspectionPolicy.from(new MapConfiguration(
                Map.of("inspection.thresholds.pass-max", 5, "inspection.thresholds.review-max", 20)));
        InspectionService service = new InspectionService(
                policy, Clock.fixed(Instant.parse("2026-08-26T00:00:00Z"), ZoneOffset.UTC));
        mvc = MockMvcBuilders.standaloneSetup(new InspectionController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void createsRetrievesAndListsInspection() throws Exception {
        String location = mvc.perform(post("/api/v1/inspections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"waferId\":\"WAFER-42\",\"defectCount\":7}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.waferId").value("WAFER-42"))
                .andExpect(jsonPath("$.disposition").value("REVIEW"))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Wafer WAFER-42: 7 defects, disposition REVIEW"));
        mvc.perform(get("/api/v1/inspections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].waferId").value("WAFER-42"));
    }

    @Test
    void returnsProblemDetailsForValidationMalformedJsonAndMissingId() throws Exception {
        mvc.perform(post("/api/v1/inspections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"waferId\":\"bad id\",\"defectCount\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.errors").isArray());

        mvc.perform(post("/api/v1/inspections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed JSON"));

        mvc.perform(get("/api/v1/inspections/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Inspection not found"));
    }
}

