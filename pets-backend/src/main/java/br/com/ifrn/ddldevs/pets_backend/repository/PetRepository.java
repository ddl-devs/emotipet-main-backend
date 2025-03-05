package br.com.ifrn.ddldevs.pets_backend.repository;

import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long>, JpaSpecificationExecutor<Pet> {
    Page<Pet> findAll(Pageable pageable);
}
