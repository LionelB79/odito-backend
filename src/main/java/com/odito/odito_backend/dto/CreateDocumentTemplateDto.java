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

    private Map<String, String> variablesDynamiques;
    private Map<String, String> variablesStatiques;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateFin;
}
