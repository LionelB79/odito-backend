package com.odito.odito_backend.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplateProcessingService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    /**
     * Extrait toutes les variables nécessaires d'un template
     * @param sectionsTemplate Map des sections avec leurs phrases
     * @return Map avec section → liste des variables à remplir
     */
    public Map<String, List<String>> extractRequiredVariables(Map<String, String> sectionsTemplate) {
        Map<String, List<String>> requiredVariables = new HashMap<>();

        for (Map.Entry<String, String> section : sectionsTemplate.entrySet()) {
            String sectionName = section.getKey();
            String content = section.getValue();

            Set<String> variables = extractVariablesFromText(content);
            requiredVariables.put(sectionName, new ArrayList<>(variables));
        }

        return requiredVariables;
    }

    /**
     * Extrait les variables d'un texte (ex: "Bonjour {{nom}}" → ["nom"])
     */
    private Set<String> extractVariablesFromText(String text) {
        Set<String> variables = new HashSet<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(text);

        while (matcher.find()) {
            variables.add(matcher.group(1).trim());
        }

        return variables;
    }

    /**
     * Remplace les variables dans le texte avec les valeurs fournies
     * @param template Texte avec variables {{variable}}
     * @param values Map des valeurs à remplacer
     * @return Texte avec variables remplacées
     */
    public String replaceVariables(String template, Map<String, String> values) {
        String result = template;

        for (Map.Entry<String, String> entry : values.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "[NON RENSEIGNÉ]";
            result = result.replace(placeholder, value);
        }

        return result;
    }

    /**
     * Génère le document final en remplaçant toutes les variables
     */
    public Map<String, String> generateDocument(Map<String, String> sectionsTemplate,
                                                Map<String, String> userValues,
                                                Map<String, String> staticValues) {
        Map<String, String> finalDocument = new HashMap<>();

        // Combiner les valeurs utilisateur et statiques
        Map<String, String> allValues = new HashMap<>();
        if (staticValues != null) allValues.putAll(staticValues);
        if (userValues != null) allValues.putAll(userValues);

        // Ajouter les variables système automatiques
        addSystemVariables(allValues);

        // Remplacer les variables dans chaque section
        for (Map.Entry<String, String> section : sectionsTemplate.entrySet()) {
            String sectionName = section.getKey();
            String sectionTemplate = section.getValue();
            String finalContent = replaceVariables(sectionTemplate, allValues);
            finalDocument.put(sectionName, finalContent);
        }

        return finalDocument;
    }

    /**
     * Ajoute les variables système automatiques (dates, etc.)
     */
    private void addSystemVariables(Map<String, String> values) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // Différents formats de date
        values.put("date_aujourd_hui", today.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        values.put("date_courante", today.format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.FRENCH)));
        values.put("date_iso", today.toString());
        values.put("annee_courante", String.valueOf(today.getYear()));
        values.put("mois_courant", today.format(java.time.format.DateTimeFormatter.ofPattern("MMMM", java.util.Locale.FRENCH)));
        values.put("jour_courant", String.valueOf(today.getDayOfMonth()));

        // Date et heure complète
        values.put("datetime_maintenant", now.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
        values.put("heure_courante", now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
    }

    /**
     * Récupère toutes les variables uniques nécessaires pour un template
     */
    public Set<String> getAllRequiredVariables(Map<String, String> sectionsTemplate) {
        Set<String> allVariables = new HashSet<>();

        for (String content : sectionsTemplate.values()) {
            allVariables.addAll(extractVariablesFromText(content));
        }

        return allVariables;
    }
}
