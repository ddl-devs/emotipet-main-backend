package br.com.ifrn.ddldevs.pets_backend.domain;


import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Gender;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Species;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
@Table(name = "pets")
public class Pet extends BaseEntity {

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birthdate;

    @Column(length = 128)
    private String breed;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Species species;

    private BigDecimal weight;

    private Integer height;

    @Column(length = 256)
    private String photoUrl;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "pet")
    @Column(insertable = false, updatable = false)
    private List<PetAnalysis> petAnalysis = new ArrayList<>();

    @OneToMany(mappedBy = "pet")
    @Column(insertable = false, updatable = false)
    private List<Recommendation> recommendations = new ArrayList<>();
}
