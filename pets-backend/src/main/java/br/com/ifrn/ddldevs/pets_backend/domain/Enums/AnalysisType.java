package br.com.ifrn.ddldevs.pets_backend.domain.Enums;

public enum AnalysisType {
    EMOTIONAL("Emoção"),
    BREED("Raça");

    private String description;

    private AnalysisType(String description) { this.description = description; }

    public String getDescription() { return description; }
}
