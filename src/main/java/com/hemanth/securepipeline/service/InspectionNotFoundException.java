package com.hemanth.securepipeline.service;

import java.util.UUID;

public class InspectionNotFoundException extends RuntimeException {
    public InspectionNotFoundException(UUID id) {
        super("Inspection " + id + " was not found");
    }
}

