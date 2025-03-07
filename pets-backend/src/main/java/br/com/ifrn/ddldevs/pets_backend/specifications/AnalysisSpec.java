package br.com.ifrn.ddldevs.pets_backend.specifications;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisStatus;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisType;
import br.com.ifrn.ddldevs.pets_backend.domain.PetAnalysis;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class AnalysisSpec {
    public static Specification<PetAnalysis> hasAnalysisType(AnalysisType analysisType) {
        return (root, query, criteriaBuilder) -> {
            if (analysisType == null) return null;
            return criteriaBuilder.equal(root.get("analysisType"), analysisType);
        };
    }

    public static Specification<PetAnalysis> hasStartDateAfter(LocalDate startDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null) return null;
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay());
        };
    }

    public static Specification<PetAnalysis> hasEndDateBefore(LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (endDate == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(23, 59, 59));
        };
    }

    public static Specification<PetAnalysis> hasResultContaining(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isEmpty()) return null;
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("result")),
                    "%" + keyword.toLowerCase() + "%"
            );
        };
    }

    public static Specification<PetAnalysis> hasAnalysisStatus(AnalysisStatus analysisStatus) {
        return (root, query, criteriaBuilder) -> {
            if (analysisStatus == null) return null;
            return criteriaBuilder.equal(root.get("analysisStatus"), analysisStatus);
        };
    }

    public static Specification<PetAnalysis> hasPetId(Long petId) {
        return (root, query, criteriaBuilder) -> {
            if (petId == null) return null;
            return criteriaBuilder.equal(root.get("pet").get("id"), petId);
        };
    }
}
