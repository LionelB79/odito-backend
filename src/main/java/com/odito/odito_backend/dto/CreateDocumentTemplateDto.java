package com.odito.odito_backend.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDocumentTemplateDto {

    @NotBlank(message = "Le titre ne peut pas être vide")
    private String titre;

    private String description;
    private String categorie;

    // Données fixes de l'entreprise (configurées par l'admin)
    private Map<String, String> donneesEntreprise;

    // Template avec placeholders {{variable}} (phrases statiques + variables dynamiques)
    private Map<String, String> contenuTemplate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateFin;
}
