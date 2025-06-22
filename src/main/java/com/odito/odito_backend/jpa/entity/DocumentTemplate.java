package com.odito.odito_backend.jpa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "document_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre ne peut pas être vide")
    @Column(nullable = false)
    private String titre;

    @Column(name = "description")
    private String description;

    @Column(name = "categorie")
    private String categorie;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "donnees_entreprise", columnDefinition = "jsonb")
    private Map<String, String> donneesEntreprise = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contenu_template", columnDefinition = "jsonb")
    private Map<String, String> contenuTemplate = new HashMap<>();

    @NotNull
    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (dateDebut == null) {
            dateDebut = LocalDateTime.now();
        }
    }
}