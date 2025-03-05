package br.com.ifrn.ddldevs.pets_backend.specifications;

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
}
