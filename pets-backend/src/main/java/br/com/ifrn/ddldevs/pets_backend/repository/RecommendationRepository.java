package br.com.ifrn.ddldevs.pets_backend.repository;

import br.com.ifrn.ddldevs.pets_backend.domain.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long>, JpaSpecificationExecutor<Recommendation> {
    Page<Recommendation> findAll(Pageable pageable);
}