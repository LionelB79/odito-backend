package com.odito.odito_backend.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentGenerationService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    /**
     * Extrait toutes les variables que l'utilisateur doit renseigner
     * (exclut les variables système automatiques)
     */
    public Set<String> extractUserVariables(Map<String, String> contenuTemplate) {
        Set<String> userVariables = new HashSet<>();

        for (String content : contenuTemplate.values()) {
            Matcher matcher = VARIABLE_PATTERN.matcher(content);
            while (matcher.find()) {
                String variable = matcher.group(1).trim();
                // Exclure les variables système automatiques
                if (!isSystemVariable(variable)) {
                    userVariables.add(variable);
                }
            }
        }

        return userVariables;
    }

    /**
     * Extrait les variables par section (pour affichage organisé)
     */
    public Map<String, Set<String>> extractVariablesBySection(Map<String, String> contenuTemplate) {
        Map<String, Set<String>> variablesBySection = new HashMap<>();

        for (Map.Entry<String, String> section : contenuTemplate.entrySet()) {
            Set<String> sectionVariables = new HashSet<>();
            Matcher matcher = VARIABLE_PATTERN.matcher(section.getValue());

            while (matcher.find()) {
                String variable = matcher.group(1).trim();
                if (!isSystemVariable(variable)) {
                    sectionVariables.add(variable);
                }
            }

            if (!sectionVariables.isEmpty()) {
                variablesBySection.put(section.getKey(), sectionVariables);
            }
        }

        return variablesBySection;
    }

    /**
     * Génère le document final
     * Ordre de priorité : Variables système < Données entreprise < Données utilisateur
     */
    public Map<String, String> generateDocument(
            Map<String, String> contenuTemplate,
            Map<String, String> donneesEntreprise,
            Map<String, String> donneesUtilisateur) {

        Map<String, String> allValues = new HashMap<>();

        // 1. Variables système (priorité la plus basse)
        addSystemVariables(allValues);

        // 2. Données entreprise (priorité moyenne)
        if (donneesEntreprise != null) {
            allValues.putAll(donneesEntreprise);
        }

        // 3. Données utilisateur (priorité la plus haute)
        if (donneesUtilisateur != null) {
            allValues.putAll(donneesUtilisateur);
        }

        // 4. Remplacer dans chaque section
        Map<String, String> finalDocument = new HashMap<>();
        for (Map.Entry<String, String> section : contenuTemplate.entrySet()) {
            String finalContent = replaceVariables(section.getValue(), allValues);
            finalDocument.put(section.getKey(), finalContent);
        }

        return finalDocument;
    }

    /**
     * Remplace les variables dans un texte
     */
    private String replaceVariables(String template, Map<String, String> values) {
        String result = template;

        for (Map.Entry<String, String> entry : values.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "[NON RENSEIGNÉ]";
            result = result.replace(placeholder, value);
        }

        return result;
    }

    /**
     * Ajoute les variables système automatiques
     */
    private void addSystemVariables(Map<String, String> values) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // Variables de date
        values.put("date_aujourd_hui", today.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        values.put("date_courante", today.format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.FRENCH)));
        values.put("date_iso", today.toString());
        values.put("annee_courante", String.valueOf(today.getYear()));
        values.put("mois_courant", today.format(java.time.format.DateTimeFormatter.ofPattern("MMMM", java.util.Locale.FRENCH)));
        values.put("jour_courant", String.valueOf(today.getDayOfMonth()));

        // Variables de temps
        values.put("datetime_maintenant", now.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
        values.put("heure_courante", now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
    }

    /**
     * Vérifie si une variable est une variable système automatique
     */
    private boolean isSystemVariable(String variable) {
        return variable.startsWith("date_") ||
                variable.startsWith("annee_") ||
                variable.startsWith("mois_") ||
                variable.startsWith("jour_") ||
                variable.equals("datetime_maintenant") ||
                variable.equals("heure_courante");
    }

    /**
     * Valide qu'un template est bien formé - VERSION SIMPLIFIÉE ET CORRIGÉE
     */
    public List<String> validateTemplate(Map<String, String> contenuTemplate) {
        List<String> errors = new ArrayList<>();

        if (contenuTemplate == null || contenuTemplate.isEmpty()) {
            return errors; // Pas d'erreur si vide
        }

        for (Map.Entry<String, String> section : contenuTemplate.entrySet()) {
            String content = section.getValue();
            if (content == null || content.trim().isEmpty()) {
                continue; // Ignore les sections vides
            }

            String sectionName = section.getKey();

            // Méthode simplifiée : analyser caractère par caractère
            List<String> sectionErrors = validateSectionContent(sectionName, content);
            errors.addAll(sectionErrors);
        }

        return errors;
    }

    /**
     * Valide le contenu d'une section en analysant caractère par caractère
     */
    private List<String> validateSectionContent(String sectionName, String content) {
        List<String> errors = new ArrayList<>();

        int i = 0;
        int openBraceCount = 0;
        int closeBraceCount = 0;

        while (i < content.length()) {
            char current = content.charAt(i);

            if (current == '{') {
                openBraceCount++;

                // Vérifier si c'est le début d'une variable {{
                if (i + 1 < content.length() && content.charAt(i + 1) == '{') {
                    // C'est une variable potentielle {{
                    int variableStart = i;
                    i += 2; // passer les deux {{

                    // Chercher la fin de la variable }}
                    StringBuilder variableName = new StringBuilder();
                    boolean foundEnd = false;

                    while (i < content.length()) {
                        char varChar = content.charAt(i);

                        if (varChar == '}' && i + 1 < content.length() && content.charAt(i + 1) == '}') {
                            // Trouvé la fin }}
                            foundEnd = true;
                            closeBraceCount += 2;
                            i += 2; // passer les deux }}
                            break;
                        } else if (varChar == '{') {
                            // Accolade inattendue dans le nom de variable
                            errors.add("Section '" + sectionName + "': accolade inattendue dans le nom de variable");
                            break;
                        } else {
                            variableName.append(varChar);
                            i++;
                        }
                    }

                    if (!foundEnd) {
                        errors.add("Section '" + sectionName + "': variable non fermée détectée");
                    } else {
                        // Vérifier que le nom de variable n'est pas vide
                        if (variableName.toString().trim().isEmpty()) {
                            errors.add("Section '" + sectionName + "': variable vide détectée {{}}");
                        }
                    }

                    openBraceCount++; // pour le deuxième {
                } else {
                    // Accolade simple orpheline
                    errors.add("Section '" + sectionName + "': accolade orpheline détectée");
                    i++;
                }
            } else if (current == '}') {
                closeBraceCount++;

                // Vérifier si c'est une accolade orpheline
                boolean isPartOfVariable = false;

                // Regarder en arrière pour voir si on est dans une séquence }}
                if (i > 0 && content.charAt(i - 1) == '}') {
                    // C'est probablement la fin d'une variable, déjà traitée
                    isPartOfVariable = true;
                }

                if (!isPartOfVariable) {
                    errors.add("Section '" + sectionName + "': accolade orpheline détectée");
                }

                i++;
            } else {
                i++;
            }
        }

        // Vérification finale de l'équilibre
        if (openBraceCount != closeBraceCount) {
            errors.add("Section '" + sectionName + "': nombre d'accolades non équilibré (" +
                    openBraceCount + " ouvrantes, " + closeBraceCount + " fermantes)");
        }

        return errors;
    }


}