package br.com.ifrn.ddldevs.pets_backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String keycloakId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private LocalDate dateOfBirth;

    @Column(length = 256)
    private String photoUrl;

    @OneToMany(mappedBy = "user")
    private List<Pet> pets;
}

