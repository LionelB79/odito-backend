package com.odito.odito_backend.service;

import com.odito.odito_backend.dto.CreateDocumentTemplateDto;
import com.odito.odito_backend.dto.DocumentTemplateMapper;
import com.odito.odito_backend.dto.DocumentTemplateResponseDto;
import com.odito.odito_backend.dto.UpdateDocumentTemplateDto;
import com.odito.odito_backend.exception.DuplicateResourceException;
import com.odito.odito_backend.exception.ResourceNotFoundException;
import com.odito.odito_backend.jpa.entity.DocumentTemplate;
import com.odito.odito_backend.jpa.repository.DocumentTemplateRepository;
import lombok.RequiredArgsConstructor;

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


        private final DocumentTemplateRepository repository;
        private final DocumentTemplateMapper mapper;

        // Constructeur manuel pour être sûr
        public DocumentTemplateService(DocumentTemplateRepository repository, DocumentTemplateMapper mapper) {
            this.repository = repository;
            this.mapper = mapper;
        }

        @Transactional
        public DocumentTemplateResponseDto createTemplate(CreateDocumentTemplateDto dto) {
            log.debug("Création d'un nouveau template avec le titre: {}", dto.getTitre());

            // Vérifier si le titre existe déjà
            if (repository.existsByTitreIgnoreCase(dto.getTitre())) {
                throw new DuplicateResourceException("Un template avec ce titre existe déjà: " + dto.getTitre());
            }

            try {
                DocumentTemplate template = mapper.toEntity(dto);
                DocumentTemplate savedTemplate = repository.save(template);
                log.info("Template créé avec succès avec l'ID: {}", savedTemplate.getId());
                return mapper.toResponseDto(savedTemplate);
            } catch (DataIntegrityViolationException e) {
                log.error("Erreur d'intégrité lors de la création du template", e);
                throw new RuntimeException("Erreur lors de la création du template", e);
            }
        }

        public DocumentTemplateResponseDto getTemplateById(Long id) {
            log.debug("Récupération du template avec l'ID: {}", id);
            DocumentTemplate template = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Template non trouvé avec l'ID: " + id));
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
        public DocumentTemplateResponseDto updateTemplate(Long id, UpdateDocumentTemplateDto dto) {
            log.debug("Mise à jour du template avec l'ID: {}", id);

            DocumentTemplate template = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Template non trouvé avec l'ID: " + id));

            // Vérifier si le nouveau titre existe déjà (uniquement si le titre a changé)
            if (dto.getTitre() != null && !dto.getTitre().equals(template.getTitre())) {
                if (repository.existsByTitreIgnoreCase(dto.getTitre())) {
                    throw new DuplicateResourceException("Un template avec ce titre existe déjà: " + dto.getTitre());
                }
            }

            try {
                mapper.updateEntityFromDto(template, dto);
                DocumentTemplate updatedTemplate = repository.save(template);
                log.info("Template mis à jour avec succès avec l'ID: {}", updatedTemplate.getId());
                return mapper.toResponseDto(updatedTemplate);
            } catch (DataIntegrityViolationException e) {
                log.error("Erreur d'intégrité lors de la mise à jour du template", e);
                throw new RuntimeException("Erreur lors de la mise à jour du template", e);
            }
        }

        @Transactional
        public void deleteTemplate(Long id) {
            log.debug("Suppression du template avec l'ID: {}", id);

            if (!repository.existsById(id)) {
                throw new ResourceNotFoundException("Template non trouvé avec l'ID: " + id);
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
