package br.com.ifrn.ddldevs.pets_backend.domain;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.RecommendationCategories;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "recommendations")
public class Recommendation extends BaseEntity {

    @Lob
    @Column(nullable = false)
    private String recommendation;

    @Enumerated(EnumType.STRING)
    private RecommendationCategories categoryRecommendation;

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;
}