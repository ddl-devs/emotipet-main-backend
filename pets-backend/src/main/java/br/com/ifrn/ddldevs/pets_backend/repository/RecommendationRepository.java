package br.com.ifrn.ddldevs.pets_backend.repository;

import br.com.ifrn.ddldevs.pets_backend.domain.Recommendation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findAllByPetId(Long petId);
}