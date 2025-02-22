package br.com.ifrn.ddldevs.pets_backend.domain.Enums;

public enum AnalysisType {
    DOG_EMOTIONAL("Emoção de Cachorro"),
    DOG_BREED("Raça de Cachorro"),
    CAT_EMOTIONAL("Emoção de Gato"),
    CAT_BREED("Raça de Gato");

    private String description;

    private AnalysisType(String description) { this.description = description; }

    public String getDescription() { return description; }
}
