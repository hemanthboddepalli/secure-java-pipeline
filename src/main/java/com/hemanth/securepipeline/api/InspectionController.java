package com.hemanth.securepipeline.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hemanth.securepipeline.domain.Inspection;
import com.hemanth.securepipeline.service.InspectionService;

@RestController
@RequestMapping("/api/v1/inspections")
public class InspectionController {
    private final InspectionService service;

    public InspectionController(InspectionService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<InspectionResponse> create(@Valid @RequestBody CreateInspectionRequest request) {
        Inspection inspection = service.create(request.waferId(), request.defectCount());
        return ResponseEntity
                .created(URI.create("/api/v1/inspections/" + inspection.id()))
                .body(InspectionResponse.from(inspection));
    }

    @GetMapping("/{id}")
    InspectionResponse get(@PathVariable UUID id) {
        return InspectionResponse.from(service.get(id));
    }

    @GetMapping
    List<InspectionResponse> list() {
        return service.list().stream().map(InspectionResponse::from).toList();
    }
}

