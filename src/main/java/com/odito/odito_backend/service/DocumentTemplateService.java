package com.odito.odito_backend.service;

import com.odito.odito_backend.dto.CreateDocumentTemplateDto;
import com.odito.odito_backend.dto.DocumentTemplateMapper;
import com.odito.odito_backend.dto.DocumentTemplateResponseDto;
import com.odito.odito_backend.dto.UpdateDocumentTemplateDto;
import com.odito.odito_backend.jpa.entity.DocumentTemplate;
import com.odito.odito_backend.jpa.repository.DocumentTemplateRepository;
import com.squelette.squelette_backend.exceptions.CodeMessage;
import com.squelette.squelette_backend.exceptions.NotFoundException;
import com.squelette.squelette_backend.exceptions.RequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class DocumentTemplateService {

    // Code messages pour les exceptions
    private static final CodeMessage TEMPLATE_NOT_FOUND = new CodeMessage(100, "Template non trouvé avec l'ID: %s");
    private static final CodeMessage TEMPLATE_TITLE_EXISTS = new CodeMessage(101, "Un template avec ce titre existe déjà: %s");
    private static final CodeMessage TEMPLATE_CREATION_ERROR = new CodeMessage(102, "Erreur lors de la création du template");
    private static final CodeMessage TEMPLATE_UPDATE_ERROR = new CodeMessage(103, "Erreur lors de la mise à jour du template");

    private final DocumentTemplateRepository repository;
    private final DocumentTemplateMapper mapper;

    public DocumentTemplateService(DocumentTemplateRepository repository, DocumentTemplateMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public DocumentTemplateResponseDto createTemplate(CreateDocumentTemplateDto dto) throws RequestException {
        log.debug("Création d'un nouveau template avec le titre: {}", dto.getTitre());

        // Vérifier si le titre existe déjà
        if (repository.existsByTitreIgnoreCase(dto.getTitre())) {
            throw new RequestException(TEMPLATE_TITLE_EXISTS.formatMessage(dto.getTitre()));
        }

        try {
            DocumentTemplate template = mapper.toEntity(dto);
            DocumentTemplate savedTemplate = repository.save(template);
            log.info("Template créé avec succès avec l'ID: {}", savedTemplate.getId());
            return mapper.toResponseDto(savedTemplate);
        } catch (DataIntegrityViolationException e) {
            log.error("Erreur d'intégrité lors de la création du template", e);
            throw new RequestException(TEMPLATE_CREATION_ERROR, e);
        }
    }

    public DocumentTemplateResponseDto getTemplateById(Long id) throws NotFoundException {
        log.debug("Récupération du template avec l'ID: {}", id);
        DocumentTemplate template = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(TEMPLATE_NOT_FOUND.formatMessage(id)));
        return mapper.toResponseDto(template);
    }

    public List<DocumentTemplateResponseDto> getAllTemplates() {
        log.debug("Récupération de tous les templates");
        return repository.findAll().stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public Page<DocumentTemplateResponseDto> getAllTemplates(Pageable pageable) {
        log.debug("Récupération des templates avec pagination: {}", pageable);
        return repository.findAll(pageable)
                .map(mapper::toResponseDto);
    }

    public List<DocumentTemplateResponseDto> searchTemplatesByTitle(String titre) {
        log.debug("Recherche de templates par titre: {}", titre);
        return repository.findByTitreContainingIgnoreCase(titre).stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<DocumentTemplateResponseDto> getActiveTemplates() {
        log.debug("Récupération des templates actifs");
        return repository.findActiveTemplates(LocalDateTime.now()).stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<DocumentTemplateResponseDto> getExpiredTemplates() {
        log.debug("Récupération des templates expirés");
        return repository.findExpiredTemplates(LocalDateTime.now()).stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DocumentTemplateResponseDto updateTemplate(Long id, UpdateDocumentTemplateDto dto) throws NotFoundException, RequestException {
        log.debug("Mise à jour du template avec l'ID: {}", id);

        DocumentTemplate template = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(TEMPLATE_NOT_FOUND.formatMessage(id)));

        // Vérifier si le nouveau titre existe déjà (uniquement si le titre a changé)
        if (dto.getTitre() != null && !dto.getTitre().equals(template.getTitre())) {
            if (repository.existsByTitreIgnoreCase(dto.getTitre())) {
                throw new RequestException(TEMPLATE_TITLE_EXISTS.formatMessage(dto.getTitre()));
            }
        }

        try {
            mapper.updateEntityFromDto(template, dto);
            DocumentTemplate updatedTemplate = repository.save(template);
            log.info("Template mis à jour avec succès avec l'ID: {}", updatedTemplate.getId());
            return mapper.toResponseDto(updatedTemplate);
        } catch (DataIntegrityViolationException e) {
            log.error("Erreur d'intégrité lors de la mise à jour du template", e);
            throw new RequestException(TEMPLATE_UPDATE_ERROR, e);
        }
    }

    @Transactional
    public void deleteTemplate(Long id) throws NotFoundException {
        log.debug("Suppression du template avec l'ID: {}", id);

        if (!repository.existsById(id)) {
            throw new NotFoundException(TEMPLATE_NOT_FOUND.formatMessage(id));
        }

        repository.deleteById(id);
        log.info("Template supprimé avec succès avec l'ID: {}", id);
    }

    public long countTemplates() {
        return repository.count();
    }

    public long countActiveTemplates() {
        return repository.findActiveTemplates(LocalDateTime.now()).size();
    }
}
