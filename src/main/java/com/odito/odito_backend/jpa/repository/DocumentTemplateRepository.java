package com.odito.odito_backend.jpa.repository;

import com.odito.odito_backend.jpa.entity.DocumentTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {

    // Rechercher par titre (insensible à la casse)
    List<DocumentTemplate> findByTitreContainingIgnoreCase(String titre);

    // Rechercher les templates actifs (sans date de fin ou date de fin future)
    @Query("SELECT d FROM DocumentTemplate d WHERE d.dateFin IS NULL OR d.dateFin > :now")
    List<DocumentTemplate> findActiveTemplates(@Param("now") LocalDateTime now);

    // Rechercher les templates expirés
    @Query("SELECT d FROM DocumentTemplate d WHERE d.dateFin IS NOT NULL AND d.dateFin <= :now")
    List<DocumentTemplate> findExpiredTemplates(@Param("now") LocalDateTime now);

    // Rechercher par période de création
    List<DocumentTemplate> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Vérifier si un titre existe déjà
    boolean existsByTitreIgnoreCase(String titre);

    // Rechercher par titre exact
    Optional<DocumentTemplate> findByTitreIgnoreCase(String titre);

    // Pagination avec recherche par titre
    Page<DocumentTemplate> findByTitreContainingIgnoreCase(String titre, Pageable pageable);
}