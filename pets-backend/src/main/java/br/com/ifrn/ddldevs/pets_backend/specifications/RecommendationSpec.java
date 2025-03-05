package br.com.ifrn.ddldevs.pets_backend.specifications;

import br.com.ifrn.ddldevs.pets_backend.domain.PetAnalysis;
import br.com.ifrn.ddldevs.pets_backend.domain.Recommendation;
import org.springframework.data.jpa.domain.Specification;

public class RecommendationSpec {
    public static Specification<Recommendation> hasCategory(String categoryName) {
        return (root, query, criteriaBuilder) -> {
            if (categoryName == null) return null;
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("categoryRecommendation")),
                    "%" + categoryName.toLowerCase() + "%"
            );
        };
    }
    public static Specification<Recommendation> hasPetId(Long petId) {
        return (root, query, criteriaBuilder) -> {
            if (petId == null) return null;
            return criteriaBuilder.equal(root.get("pet").get("id"), petId);
        };
    }
    public static Specification<Recommendation> hasStartDateAfter(java.time.LocalDate startDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null) return null;
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay());
        };
    }

    public static Specification<Recommendation> hasEndDateBefore(java.time.LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (endDate == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(23, 59, 59));
        };
    }
}
