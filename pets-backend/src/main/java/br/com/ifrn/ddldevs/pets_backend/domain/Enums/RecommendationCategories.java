package br.com.ifrn.ddldevs.pets_backend.domain.Enums;

public enum RecommendationCategories {
    IMC("IMC"),
    ACTIVITIES("Atividades"),
    HEALTH("Saúde"),
    TRAINING("Treinamento"),
    PRODUCTS("Produtos");

    private String description;

    private RecommendationCategories(String description) { this.description = description; }

    public String getDescription() { return description; }
}
