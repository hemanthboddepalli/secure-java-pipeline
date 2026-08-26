package com.hemanth.securepipeline.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateInspectionRequest(
        @NotBlank
        @Size(max = 32)
        @Pattern(regexp = "^[A-Z0-9-]+$", message = "must contain only uppercase letters, numbers, and hyphens")
        String waferId,
        @Min(0)
        int defectCount) {
}

