package com.odito.odito_backend.controller;


import com.odito.odito_backend.dto.CreateDocumentTemplateDto;
import com.odito.odito_backend.dto.DocumentTemplateResponseDto;
import com.odito.odito_backend.dto.UpdateDocumentTemplateDto;
import com.odito.odito_backend.service.DocumentTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DocumentTemplateController {

    private final DocumentTemplateService templateService;

    @PostMapping
    public ResponseEntity<DocumentTemplateResponseDto> createTemplate(
            @Valid @RequestBody CreateDocumentTemplateDto dto) {
        log.info("Requête de création de template reçue: {}", dto.getTitre());
        DocumentTemplateResponseDto response = templateService.createTemplate(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentTemplateResponseDto> getTemplate(@PathVariable Long id) {
        log.info("Requête de récupération de template avec ID: {}", id);
        DocumentTemplateResponseDto response = templateService.getTemplateById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentTemplateResponseDto>> getAllTemplates(
            @RequestParam(required = false) String titre,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        log.info("Requête de récupération de tous les templates - titre: {}, activeOnly: {}", titre, activeOnly);

        List<DocumentTemplateResponseDto> response;

        if (activeOnly) {
            response = templateService.getActiveTemplates();
        } else if (titre != null && !titre.trim().isEmpty()) {
            response = templateService.searchTemplatesByTitle(titre);
        } else {
            response = templateService.getAllTemplates();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<DocumentTemplateResponseDto>> getAllTemplatesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("Requête de récupération paginée - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DocumentTemplateResponseDto> response = templateService.getAllTemplates(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<DocumentTemplateResponseDto>> getActiveTemplates() {
        log.info("Requête de récupération des templates actifs");
        List<DocumentTemplateResponseDto> response = templateService.getActiveTemplates();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expired")
    public ResponseEntity<List<DocumentTemplateResponseDto>> getExpiredTemplates() {
        log.info("Requête de récupération des templates expirés");
        List<DocumentTemplateResponseDto> response = templateService.getExpiredTemplates();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<DocumentTemplateResponseDto>> searchTemplates(
            @RequestParam String titre) {
        log.info("Requête de recherche de templates par titre: {}", titre);
        List<DocumentTemplateResponseDto> response = templateService.searchTemplatesByTitle(titre);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentTemplateResponseDto> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDocumentTemplateDto dto) {
        log.info("Requête de mise à jour de template avec ID: {}", id);
        DocumentTemplateResponseDto response = templateService.updateTemplate(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        log.info("Requête de suppression de template avec ID: {}", id);
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTemplateStats() {
        log.info("Requête de statistiques des templates");

        long totalTemplates = templateService.countTemplates();
        long activeTemplates = templateService.countActiveTemplates();

        Map<String, Object> stats = Map.of(
                "totalTemplates", totalTemplates,
                "activeTemplates", activeTemplates,
                "expiredTemplates", totalTemplates - activeTemplates
        );

        return ResponseEntity.ok(stats);
    }
}
