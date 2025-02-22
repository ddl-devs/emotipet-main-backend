package br.com.ifrn.ddldevs.pets_backend.domain.Enums;

public enum AnalysisStatus {
    IN_ANALYSIS("Em Análise"),
    FAILURE("Análise Falhou"),
    COMPLETED("Concluído");

    private String description;

    private AnalysisStatus(String description) { this.description = description; }

    public String getDescription() { return description; }
}
