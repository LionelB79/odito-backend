package com.odito.odito_backend.controller;

import com.odito.odito_backend.dto.CreateDocumentTemplateDto;
import com.odito.odito_backend.dto.DocumentTemplateResponseDto;
import com.odito.odito_backend.dto.UpdateDocumentTemplateDto;
import com.odito.odito_backend.service.DocumentTemplateService;
import com.odito.odito_backend.service.DocumentGenerationService;
import com.squelette.squelette_backend.exceptions.NotFoundException;
import com.squelette.squelette_backend.exceptions.RequestException;
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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DocumentTemplateController {

    private final DocumentTemplateService templateService;
    private final DocumentGenerationService generationService;

    // ==================== CRUD BASIQUE ====================

    @PostMapping
    public ResponseEntity<DocumentTemplateResponseDto> createTemplate(
            @Valid @RequestBody CreateDocumentTemplateDto dto) throws RequestException {
        log.info("Création de template: {}", dto.getTitre());

        // Validation du template avant création
        if (dto.getContenuTemplate() != null) {
            List<String> validationErrors = generationService.validateTemplate(dto.getContenuTemplate());
            if (!validationErrors.isEmpty()) {
                log.warn("Erreurs de validation: {}", validationErrors);
                throw new IllegalArgumentException("Template invalide: " + String.join(", ", validationErrors));
            }
        }

        DocumentTemplateResponseDto response = templateService.createTemplate(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentTemplateResponseDto> getTemplate(@PathVariable Long id) throws NotFoundException {
        log.info("Récupération du template ID: {}", id);
        DocumentTemplateResponseDto response = templateService.getTemplateById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentTemplateResponseDto>> getAllTemplates(
            @RequestParam(required = false) String titre,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        log.info("Récupération des templates - titre: {}, categorie: {}, activeOnly: {}",
                titre, categorie, activeOnly);

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

    @PutMapping("/{id}")
    public ResponseEntity<DocumentTemplateResponseDto> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDocumentTemplateDto dto) throws NotFoundException, RequestException {
        log.info("Mise à jour du template ID: {}", id);

        // Validation du template si modifié
        if (dto.getContenuTemplate() != null) {
            List<String> validationErrors = generationService.validateTemplate(dto.getContenuTemplate());
            if (!validationErrors.isEmpty()) {
                log.warn("Erreurs de validation: {}", validationErrors);
                throw new IllegalArgumentException("Template invalide: " + String.join(", ", validationErrors));
            }
        }

        DocumentTemplateResponseDto response = templateService.updateTemplate(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) throws NotFoundException {
        log.info("Suppression du template ID: {}", id);
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== RECHERCHE ET FILTRAGE ====================

    @GetMapping("/active")
    public ResponseEntity<List<DocumentTemplateResponseDto>> getActiveTemplates() {
        log.info("Récupération des templates actifs");
        List<DocumentTemplateResponseDto> response = templateService.getActiveTemplates();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expired")
    public ResponseEntity<List<DocumentTemplateResponseDto>> getExpiredTemplates() {
        log.info("Récupération des templates expirés");
        List<DocumentTemplateResponseDto> response = templateService.getExpiredTemplates();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<DocumentTemplateResponseDto>> searchTemplates(
            @RequestParam String titre) {
        log.info("Recherche de templates par titre: {}", titre);
        List<DocumentTemplateResponseDto> response = templateService.searchTemplatesByTitle(titre);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<DocumentTemplateResponseDto>> getAllTemplatesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("Récupération paginée - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DocumentTemplateResponseDto> response = templateService.getAllTemplates(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTemplateStats() {
        log.info("Récupération des statistiques");

        long totalTemplates = templateService.countTemplates();
        long activeTemplates = templateService.countActiveTemplates();

        Map<String, Object> stats = Map.of(
                "totalTemplates", totalTemplates,
                "activeTemplates", activeTemplates,
                "expiredTemplates", totalTemplates - activeTemplates
        );

        return ResponseEntity.ok(stats);
    }

    // ==================== GÉNÉRATION DE DOCUMENTS ====================

    @GetMapping("/{id}/form")
    public ResponseEntity<Map<String, Object>> getTemplateForm(@PathVariable Long id) throws NotFoundException {
        log.info("Récupération du formulaire pour template ID: {}", id);
        DocumentTemplateResponseDto template = templateService.getTemplateById(id);

        // Extraire les variables que l'utilisateur doit renseigner
        Set<String> userVariables = generationService.extractUserVariables(template.getContenuTemplate());
        Map<String, Set<String>> variablesBySection = generationService.extractVariablesBySection(template.getContenuTemplate());

        Map<String, Object> form = Map.of(
                "templateId", template.getId(),
                "templateTitle", template.getTitre(),
                "templateDescription", template.getDescription() != null ? template.getDescription() : "",
                "templateCategory", template.getCategorie() != null ? template.getCategorie() : "",
                "contenuTemplate", template.getContenuTemplate(),
                "userVariables", userVariables,
                "variablesBySection", variablesBySection,
                "donneesEntreprise", template.getDonneesEntreprise()
        );

        return ResponseEntity.ok(form);
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<Map<String, Object>> validateUserData(
            @PathVariable Long id,
            @RequestBody Map<String, String> donneesUtilisateur) throws NotFoundException {
        log.info("Validation des données utilisateur pour template ID: {}", id);

        DocumentTemplateResponseDto template = templateService.getTemplateById(id);
        Set<String> requiredVariables = generationService.extractUserVariables(template.getContenuTemplate());

        // Vérifier les variables manquantes
        Set<String> missingVariables = new HashSet<>(requiredVariables);
        missingVariables.removeAll(donneesUtilisateur.keySet());

        // Vérifier les variables en trop
        Set<String> extraVariables = new HashSet<>(donneesUtilisateur.keySet());
        extraVariables.removeAll(requiredVariables);

        boolean isValid = missingVariables.isEmpty();

        Map<String, Object> validation = Map.of(
                "valid", isValid,
                "requiredVariables", requiredVariables,
                "providedVariables", donneesUtilisateur.keySet(),
                "missingVariables", missingVariables,
                "extraVariables", extraVariables
        );

        return ResponseEntity.ok(validation);
    }

    @PostMapping("/{id}/generate")
    public ResponseEntity<Map<String, Object>> generateDocument(
            @PathVariable Long id,
            @RequestBody Map<String, String> donneesUtilisateur) throws NotFoundException {
        log.info("Génération de document pour template ID: {} avec {} variables",
                id, donneesUtilisateur.size());

        DocumentTemplateResponseDto template = templateService.getTemplateById(id);

        // Générer le document final
        Map<String, String> finalDocument = generationService.generateDocument(
                template.getContenuTemplate(),
                template.getDonneesEntreprise(),
                donneesUtilisateur
        );

        Map<String, Object> result = Map.of(
                "templateId", template.getId(),
                "templateTitle", template.getTitre(),
                "templateCategory", template.getCategorie() != null ? template.getCategorie() : "",
                "generatedDocument", finalDocument,
                "donneesUtilisateur", donneesUtilisateur,
                "generationDate", LocalDateTime.now()
        );

        return ResponseEntity.ok(result);
    }
}
