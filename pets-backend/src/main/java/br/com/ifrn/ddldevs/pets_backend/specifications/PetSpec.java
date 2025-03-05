package br.com.ifrn.ddldevs.pets_backend.specifications;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Gender;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Species;
import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import org.springframework.data.jpa.domain.Specification;

public class PetSpec {
    public static Specification<Pet> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) return null;
            return criteriaBuilder.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<Pet> hasGender(Gender gender) {
        return (root, query, criteriaBuilder) -> {
            if (gender == null) return null;
            return criteriaBuilder.equal(root.get("gender"), gender);
        };
    }

    public static Specification<Pet> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) return null;
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Pet> hasSpecies(Species species) {
        return (root, query, criteriaBuilder) -> {
            if (species == null) return null;
            return criteriaBuilder.equal(root.get("species"), species);
        };
    }

    public static Specification<Pet> hasBreed(String breed) {
        return (root, query, criteriaBuilder) -> {
            if (breed == null || breed.isEmpty()) return null;
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("breed")), "%" + breed.toLowerCase() + "%");
        };
    }
}
