package br.com.ifrn.ddldevs.pets_backend.domain;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisStatus;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
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
@Table(name = "pet_analisys")
public class PetAnalysis extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(nullable = false)
    private String picture;

    @Column(nullable = false, length = 64)
    private String result;

    @Column(nullable = true)
    private Double accuracy;

    @Enumerated(EnumType.STRING)
    private AnalysisType analysisType;

    @Enumerated(EnumType.STRING)
    private AnalysisStatus analysisStatus = AnalysisStatus.IN_ANALYSIS;
}

