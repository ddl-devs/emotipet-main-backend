package br.com.ifrn.ddldevs.pets_backend.service;

import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetUpdateRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.exception.AccessDeniedException;
import br.com.ifrn.ddldevs.pets_backend.exception.ResourceNotFoundException;
import br.com.ifrn.ddldevs.pets_backend.mapper.PetMapper;
import br.com.ifrn.ddldevs.pets_backend.repository.PetRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private UploadImageService uploadImageService;

    @Transactional
    public PetResponseDTO createPet(PetRequestDTO petRequestDTO, String loggedUserKeycloakId) {
        Pet pet = petMapper.toEntity(petRequestDTO);
        var user = userRepository.findByKeycloakId(loggedUserKeycloakId).orElseThrow(() -> {
            throw new ResourceNotFoundException("Usuário não encontrado!");
        });

        pet.setUser(user);
        user.getPets().add(pet);

        String imgUrl = null;

        if (petRequestDTO.getPhotoUrl() != null) {
            imgUrl = uploadImageService.uploadImg(petRequestDTO.getPhotoUrl());

        }

        pet.setPhotoUrl(imgUrl);

        petRepository.save(pet);
        userRepository.save(user);

        return petMapper.toPetResponseDTO(pet);
    }

    public List<PetResponseDTO> listPets() {
        List<Pet> pets = petRepository.findAll();
        return petMapper.toDTOList(pets);
    }

    public PetResponseDTO updatePet(Long id, PetUpdateRequestDTO petRequestDTO,
        String loggedUserKeycloakId) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (id < 0) {
            throw new IllegalArgumentException("ID não pode ser negativo");
        }

        Pet pet = petRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado!"));

        validatePetOwnershipOrAdmin(pet, loggedUserKeycloakId);

        petMapper.updateEntityFromDTO(petRequestDTO, pet);

        if (petRequestDTO.getPhotoUrl() != null) {
            String imgUrl = uploadImageService.uploadImg(petRequestDTO.getPhotoUrl());
            pet.setPhotoUrl(imgUrl);
        }
        
        Pet petUpdated = petRepository.save(pet);

        return petMapper.toPetResponseDTO(petUpdated);
    }

    public PetResponseDTO getPet(Long id, String loggedUserKeycloakId) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (id < 0) {
            throw new IllegalArgumentException("ID não pode ser negativo");
        }

        Pet pet = petRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado"));

        validatePetOwnershipOrAdmin(pet, loggedUserKeycloakId);

        return petMapper.toPetResponseDTO(pet);
    }

    public void deletePet(Long id, String loggedUserKeycloakId) {
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

        petRepository.deleteById(id);
    }

    public void validatePetOwnershipOrAdmin(Pet pet, String loggedUserKeycloakId) {
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
