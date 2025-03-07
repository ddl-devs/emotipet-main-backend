package br.com.ifrn.ddldevs.pets_backend.service;

import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import br.com.ifrn.ddldevs.pets_backend.domain.Recommendation;
import br.com.ifrn.ddldevs.pets_backend.dto.Recommendation.RecommendationRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.Recommendation.RecommendationResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.exception.AccessDeniedException;
import br.com.ifrn.ddldevs.pets_backend.exception.BusinessException;
import br.com.ifrn.ddldevs.pets_backend.exception.ResourceNotFoundException;
import br.com.ifrn.ddldevs.pets_backend.mapper.RecommendationMapper;
import br.com.ifrn.ddldevs.pets_backend.microservice.RecommendationRequestsService;
import br.com.ifrn.ddldevs.pets_backend.repository.PetAnalysisRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.PetRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.RecommendationRepository;

import java.time.LocalDate;
import java.util.List;

import br.com.ifrn.ddldevs.pets_backend.specifications.RecommendationSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class RecommendationService {

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private RecommendationMapper recommendationMapper;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private PetAnalysisRepository petAnalysisRepository;

    @Autowired
    private RecommendationRequestsService recommendationRequestsService;

    private final RestTemplate restTemplate = new RestTemplate();


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

        validateRecommendationData(recommendation);

        recommendationRequestsService.updateRecommendation(recommendation);

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
    public Page<RecommendationResponseDTO> getAllByPetId(Long id, String loggedUserKeycloakId, Pageable page,
                                                         String category, LocalDate startDate, LocalDate endDate) {
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
        Specification<Recommendation> spec = Specification.where(RecommendationSpec.hasPetId(id))
                .and(RecommendationSpec.hasCategory(category))
                .and(RecommendationSpec.hasStartDateAfter(startDate))
                .and(RecommendationSpec.hasEndDateBefore(endDate));

        Page<Recommendation> recommendations = recommendationRepository.findAll(spec, page);
        return recommendations.map(recommendationMapper::toRecommendationResponseDTO);
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

    private void validateRecommendationData(Recommendation recommendation) {
        switch (recommendation.getCategoryRecommendation()){
            case HEALTH:
            case ACTIVITIES:
            case TRAINING:
            case PRODUCTS:
                if(recommendation.getPet().getBreed() == null && recommendation.getPet().getSpecies() == null) {
                    throw new BusinessException("É necessário pelo menos a raça/espécie para criar uma " +
                            "recomendação nesta categoria");
                }
                break;
            case IMC:
                if(recommendation.getPet().getBreed() == null && recommendation.getPet().getSpecies() == null) {
                    throw new BusinessException("É necessário pelo menos a raça/espécie para criar uma " +
                            "recomendação nesta categoria");
                }
                if(recommendation.getPet().getWeight() == null || recommendation.getPet().getHeight() == null) {
                    throw new BusinessException("É necessário a altura e peso para calcular o IMC");
                }
                break;
            default:
                throw new BusinessException("Categoria inválida");
        }
    }
}