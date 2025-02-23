package br.com.ifrn.ddldevs.pets_backend.repository;

import br.com.ifrn.ddldevs.pets_backend.domain.PetAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PetAnalysisRepository extends JpaRepository<PetAnalysis, Long> {
    List<PetAnalysis> findAllByPetId(Long petId);
    @Query("""
        SELECT p FROM PetAnalysis p 
        WHERE p.pet.id = :petId 
        AND p.createdAt >= :oneMonthAgo 
        AND (p.analysisType = 'DOG_EMOTIONAL' OR p.analysisType = 'CAT_EMOTIONAL')
        AND (p.analysisStatus = 'COMPLETED')
        ORDER BY p.createdAt DESC
    """)
    List<PetAnalysis> findRecentEmotionalAnalyses(@Param("petId") Long petId, @Param("oneMonthAgo") LocalDateTime oneMonthAgo);
}
