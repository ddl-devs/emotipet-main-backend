package br.com.ifrn.ddldevs.pets_backend.service;

import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import br.com.ifrn.ddldevs.pets_backend.domain.Recommendation;
import br.com.ifrn.ddldevs.pets_backend.dto.Recommendation.RecommendationRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.Recommendation.RecommendationResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.exception.AccessDeniedException;
import br.com.ifrn.ddldevs.pets_backend.exception.ResourceNotFoundException;
import br.com.ifrn.ddldevs.pets_backend.mapper.RecommendationMapper;
import br.com.ifrn.ddldevs.pets_backend.repository.PetRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.RecommendationRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationService {

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private RecommendationMapper recommendationMapper;

    @Autowired
    private PetRepository petRepository;

    @Transactional
    public RecommendationResponseDTO createRecommendation(
        RecommendationRequestDTO recommendationRequestDTO,
        String loggedUserKeycloakId
    ) {
        Long idPet = recommendationRequestDTO.getPetId();
        if (idPet == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (idPet < 0) {
            throw new IllegalArgumentException("ID não pode ser negativo");
        }

        Recommendation recommendation = recommendationMapper.toEntity(recommendationRequestDTO);
        Pet pet = petRepository.findById(recommendationRequestDTO.getPetId())
            .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado!"));

        validatePetOwnershipOrAdmin(pet, loggedUserKeycloakId);

        recommendation.setPet(pet);
        pet.getRecommendations().add(recommendation);

        recommendationRepository.save(recommendation);
        petRepository.save(pet);

        return recommendationMapper.toRecommendationResponseDTO(recommendation);
    }

    public List<RecommendationResponseDTO> listRecommendations() {
        List<Recommendation> recommendations = recommendationRepository.findAll();
        return recommendationMapper.toDTOList(recommendations);
    }

    public RecommendationResponseDTO getRecommendation(Long id, String loggedUserKeycloakId) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (id < 0) {
            throw new IllegalArgumentException("ID não pode ser negativo");
        }

        Recommendation recommendation = recommendationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recomendação não encontrada!"));

        validatePetOwnershipOrAdmin(recommendation.getPet(), loggedUserKeycloakId);

        return recommendationMapper.toRecommendationResponseDTO(recommendation);
    }

    public void deleteRecommendation(Long id, String loggedUserKeycloakId) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (id < 0) {
            throw new IllegalArgumentException("ID não pode ser negativo");
        }

        Recommendation recommendation = recommendationRepository.findById(id).orElseThrow(() -> {
            throw new ResourceNotFoundException("Recomendação não encontrada!");
        });

        validatePetOwnershipOrAdmin(recommendation.getPet(), loggedUserKeycloakId);

        recommendationRepository.deleteById(id);
    }

    @Transactional
    public List<RecommendationResponseDTO> getAllByPetId(Long id, String loggedUserKeycloakId) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (id < 0) {
            throw new IllegalArgumentException("ID não pode ser negativo");
        }

        Pet pet = petRepository.findById(id).orElseThrow(() -> {
            throw new ResourceNotFoundException("Pet não encontrado!");
        });

        validatePetOwnershipOrAdmin(pet, loggedUserKeycloakId);

        List<Recommendation> recommendations = recommendationRepository.findAllByPetId(id);

        return recommendationMapper.toDTOList(recommendations);
    }

    private void validatePetOwnershipOrAdmin(Pet pet, String loggedUserKeycloakId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getAuthorities().stream()
            .anyMatch(
                grantedAuthority ->
                    grantedAuthority.getAuthority().equals("ROLE_admin"))) {
            return;
        }

        if (!pet.getUser().getKeycloakId().equals(loggedUserKeycloakId)) {
            throw new AccessDeniedException(
                "Você não pode acessar dados de pets de outros usuários!");
        }
    }
}