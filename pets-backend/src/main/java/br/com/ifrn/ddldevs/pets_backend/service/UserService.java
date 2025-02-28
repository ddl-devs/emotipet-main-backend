package br.com.ifrn.ddldevs.pets_backend.service;

import br.com.ifrn.ddldevs.pets_backend.domain.User;
import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.User.UserRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.User.UserResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.User.UserUpdateRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.KcUserResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.exception.AccessDeniedException;
import br.com.ifrn.ddldevs.pets_backend.exception.ResourceNotFoundException;
import br.com.ifrn.ddldevs.pets_backend.keycloak.KeycloakServiceImpl;
import br.com.ifrn.ddldevs.pets_backend.mapper.PetMapper;
import br.com.ifrn.ddldevs.pets_backend.mapper.UserMapper;
import br.com.ifrn.ddldevs.pets_backend.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private KeycloakServiceImpl keycloakServiceImpl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private UploadImageService uploadImageService;

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO dto) {
        KcUserResponseDTO keycloakUser = keycloakServiceImpl.createUser(dto);

        User user = userMapper.toEntity(dto);
        user.setKeycloakId(keycloakUser.id());

        String imgUrl = null;

        if (dto.photoUrl() != null) {
            imgUrl = uploadImageService.uploadImg(dto.photoUrl());
        }

        user.setPhotoUrl(imgUrl);

        userRepository.save(user);
        return userMapper.toResponseDTO(user);
    }

    public List<UserResponseDTO> listUsers() {
        var users = userRepository.findAll();

        return userMapper.toDTOList(users);
    }

    public UserResponseDTO getCurrentUser(String loggedUserKeycloakId) {
        var user = userRepository.findByKeycloakId(loggedUserKeycloakId).orElseThrow(() -> {
            throw new ResourceNotFoundException("Usuário não encontrado!");
        });
        return userMapper.toResponseDTO(user);
    }

    public UserResponseDTO getUserById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (id < 0) {
            throw new IllegalArgumentException("ID não pode ser negativo");
        }

        User user = userRepository.findById(id).
            orElseThrow(() -> new ResourceNotFoundException("Usuário não existe"));

        return userMapper.toResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UserUpdateRequestDTO dto,
        String loggedUserKeycloakId) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (id < 0) {
            throw new IllegalArgumentException("ID não pode ser negativo");
        }

        User user = userRepository.findById(id).
            orElseThrow(() -> new ResourceNotFoundException("Usuário não existe"));

        validatePetOwnershipOrAdmin(loggedUserKeycloakId, user);

        keycloakServiceImpl.updateUser(user.getKeycloakId(), dto);

        userMapper.updateEntityFromDTO(dto, user);

        if (dto.photoUrl() != null) {
            String imgUrl = uploadImageService.uploadImg(dto.photoUrl());
            user.setPhotoUrl(imgUrl);
        }
        
        var updatedUser = userRepository.save(user);

        return userMapper.toResponseDTO(updatedUser);
    }

    public void deleteUser(Long id, String loggedUserKeycloakId) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (id < 0) {
            throw new IllegalArgumentException("ID não pode ser negativo");
        }

        User user = userRepository.findById(id).orElseThrow(
            () -> new ResourceNotFoundException("Usuário não encontrado!")
        );

        validatePetOwnershipOrAdmin(loggedUserKeycloakId, user);

        keycloakServiceImpl.deleteUser(user.getKeycloakId());

        userRepository.deleteById(id);
    }

//    public List<PetResponseDTO> getPetsOfCurrentUser(String loggedUserKeycloakId) {
//        var user = userRepository.findByKeycloakId(loggedUserKeycloakId).orElseThrow(() -> {
//            throw new ResourceNotFoundException("Usuário não encontrado!");
//        });
//
//        return petMapper.toDTOList(user.getPets());
//    }

    public List<PetResponseDTO> getPets(String userKcId) {
        User user = userRepository.findByKeycloakId(userKcId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        return petMapper.toDTOList(user.getPets());
    }

    public void validatePetOwnershipOrAdmin(
        String loggedPersonKeycloakId,
        User user
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getAuthorities().stream()
            .anyMatch(
                grantedAuthority ->
                    grantedAuthority.getAuthority().equals("ROLE_admin"))) {
            return;
        }

        if (!user.getKeycloakId().equals(loggedPersonKeycloakId)) {
            throw new AccessDeniedException(
                "Você não pode acessar dados de outros usuários!"
            );
        }
    }
}
