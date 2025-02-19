package br.com.ifrn.ddldevs.pets_backend.domain;

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

    @Column(nullable = false, length = 64, name = "analysis_type")
    private String analysisType;
}

