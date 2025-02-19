package br.com.ifrn.ddldevs.pets_backend.domain;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisStatus;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisType;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pet_analisys")
public class PetAnalysis extends BaseEntity{
    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(nullable = false)
    private String picture;

    @Column(nullable = false, length = 64)
    private String result;

    @Enumerated(EnumType.STRING)
    private AnalysisType analysisType;

    @Enumerated(EnumType.STRING)
    private AnalysisStatus analysisStatus = AnalysisStatus.IN_ANALYSIS;
}

