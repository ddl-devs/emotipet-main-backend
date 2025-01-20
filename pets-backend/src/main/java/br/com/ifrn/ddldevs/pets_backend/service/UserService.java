package br.com.ifrn.ddldevs.pets_backend.service;

import br.com.ifrn.ddldevs.pets_backend.domain.User;
import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.KcUserResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.pet.PetResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.user.UserRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.user.UserResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.exception.ResourceNotFoundException;
import br.com.ifrn.ddldevs.pets_backend.keycloak.KeycloakService;
import br.com.ifrn.ddldevs.pets_backend.mapper.PetMapper;
import br.com.ifrn.ddldevs.pets_backend.mapper.UserMapper;
import br.com.ifrn.ddldevs.pets_backend.repository.UserRepository;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private KeycloakService keycloakService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PetMapper petMapper;

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO dto) {
        KcUserResponseDTO keycloakUser = keycloakService.createUser(dto);

        User user = userMapper.toEntity(dto);
        user.setKeycloakId(keycloakUser.id());

        userRepository.save(user);
        return userMapper.toResponseDTO(user);
    }

    public List<UserResponseDTO> listUsers() {
        var users = userRepository.findAll();

        return userMapper.toDTOList(users);
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
    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (id < 0) {
            throw new IllegalArgumentException("ID não pode ser negativo");
        }

        User user = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Usuário não existe"));

        keycloakService.updateUser(user.getKeycloakId(), dto);

        userMapper.updateEntityFromDTO(dto, user);

        var updatedUser = userRepository.save(user);

        return userMapper.toResponseDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (id < 0) {
            throw new IllegalArgumentException("ID não pode ser negativo");
        }
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }
        userRepository.deleteById(id);
    }

    public List<PetResponseDTO> getPets(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        return petMapper.toDTOList(user.getPets());
    }
}
