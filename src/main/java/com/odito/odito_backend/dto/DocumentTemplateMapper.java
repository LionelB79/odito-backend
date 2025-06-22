package com.odito.odito_backend.dto;


import com.odito.odito_backend.jpa.entity.DocumentTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;

@Component
public class DocumentTemplateMapper {

    public DocumentTemplate toEntity(CreateDocumentTemplateDto dto) {
        DocumentTemplate template = new DocumentTemplate();
        template.setTitre(dto.getTitre());
        template.setDescription(dto.getDescription());
        template.setCategorie(dto.getCategorie());
        template.setDonneesEntreprise(dto.getDonneesEntreprise() != null ?
                dto.getDonneesEntreprise() : new HashMap<>());
        template.setContenuTemplate(dto.getContenuTemplate() != null ?
                dto.getContenuTemplate() : new HashMap<>());
        template.setDateDebut(LocalDateTime.now());
        template.setDateFin(dto.getDateFin());
        return template;
    }

    public DocumentTemplateResponseDto toResponseDto(DocumentTemplate template) {
        return DocumentTemplateResponseDto.builder()
                .id(template.getId())
                .titre(template.getTitre())
                .description(template.getDescription())
                .categorie(template.getCategorie())
                .donneesEntreprise(template.getDonneesEntreprise())
                .contenuTemplate(template.getContenuTemplate())
                .dateDebut(template.getDateDebut())
                .dateFin(template.getDateFin())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    public void updateEntityFromDto(DocumentTemplate template, UpdateDocumentTemplateDto dto) {
        if (dto.getTitre() != null && !dto.getTitre().trim().isEmpty()) {
            template.setTitre(dto.getTitre());
        }
        if (dto.getDescription() != null) {
            template.setDescription(dto.getDescription());
        }
        if (dto.getCategorie() != null) {
            template.setCategorie(dto.getCategorie());
        }
        if (dto.getDonneesEntreprise() != null) {
            template.setDonneesEntreprise(dto.getDonneesEntreprise());
        }
        if (dto.getContenuTemplate() != null) {
            template.setContenuTemplate(dto.getContenuTemplate());
        }
        template.setDateFin(dto.getDateFin());
    }
}