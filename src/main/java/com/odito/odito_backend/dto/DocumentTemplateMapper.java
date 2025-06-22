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
        template.setVariablesDynamiques(dto.getVariablesDynamiques() != null ?
                dto.getVariablesDynamiques() : new HashMap<>());
        template.setVariablesStatiques(dto.getVariablesStatiques() != null ?
                dto.getVariablesStatiques() : new HashMap<>());
        template.setDateDebut(LocalDateTime.now());
        template.setDateFin(dto.getDateFin());
        return template;
    }

    public DocumentTemplateResponseDto toResponseDto(DocumentTemplate template) {
        return DocumentTemplateResponseDto.builder()
                .id(template.getId())
                .titre(template.getTitre())
                .variablesDynamiques(template.getVariablesDynamiques())
                .variablesStatiques(template.getVariablesStatiques())
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
        if (dto.getVariablesDynamiques() != null) {
            template.setVariablesDynamiques(dto.getVariablesDynamiques());
        }
        if (dto.getVariablesStatiques() != null) {
            template.setVariablesStatiques(dto.getVariablesStatiques());
        }
        template.setDateFin(dto.getDateFin());
    }
}
