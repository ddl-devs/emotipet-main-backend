package br.com.ifrn.ddldevs.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Species;
import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import br.com.ifrn.ddldevs.pets_backend.domain.User;
import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.User.UserRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.User.UserResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.User.UserUpdateRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.keycloak.KcUserResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.keycloak.KeycloakServiceImpl;
import br.com.ifrn.ddldevs.pets_backend.mapper.PetMapper;
import br.com.ifrn.ddldevs.pets_backend.mapper.UserMapper;
import br.com.ifrn.ddldevs.pets_backend.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {

    @Mock
    private KeycloakServiceImpl keycloakServiceImpl;

    @Mock
    private PetMapper petMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    private Validator validator;

    private final String loggedUserKeycloakId = "1abc23";

    @InjectMocks
    UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // a

    @Test
    void shouldPassValidationForValidUserRequestDTO() {
        UserRequestDTO userRequestDTO = new UserRequestDTO(
            "john", "john@email.com",
            "John", "Doe",
            LocalDate.of(1990, 1, 15),
            "aws.12bs.bucket.com", "user!123"
        );

        Set<ConstraintViolation<UserRequestDTO>> violations =
            validator.validate(userRequestDTO);

        assertTrue(violations.isEmpty(), "Expected no validation errors");
    }

    @Test
    void shouldFailValidationWhenFieldsAreNull() {
        UserRequestDTO userRequestDTO = new UserRequestDTO(
            "john",
            null, null, null,
            LocalDate.of(1990, 1, 15),
            "aws.12bs.bucket.com", null
        );

        Set<ConstraintViolation<UserRequestDTO>> violations =
            validator.validate(userRequestDTO);

        assertFalse(violations.isEmpty(), "Expected validation errors");
        assertEquals(4, violations.size());
    }

    @Test
    void shouldFailValidationEmailInvalid() {
        UserRequestDTO userRequestDTO = new UserRequestDTO(
            "john", "john123email.com",
            "John", "Doe",
            LocalDate.of(1990, 1, 15),
            "aws.12bs.bucket.com", "user!123"
        );

        Set<ConstraintViolation<UserRequestDTO>> violations =
            validator.validate(userRequestDTO);

        assertFalse(violations.isEmpty(), "Expected validation errors");
        assertEquals(1, violations.size(), "Expected exactly one validation error");
    }

    @Test
    void shouldFailWhenUsernameAndEmailExists() {
        User existingUser = new User("1ab23", "john",
            "John", "Doe", "john@email.com",
            LocalDate.of(1990, 1, 15),
            "www.foto.url", new ArrayList<>());

        User duplicateUser = new User("1abc23", "john",
            "John", "Doe", "john@email.com",
            LocalDate.of(1990, 1, 15),
            "www.foto.url", new ArrayList<>());

        when(userRepository.save(existingUser)).thenReturn(existingUser);

        userRepository.save(existingUser);

        when(userRepository.save(duplicateUser)).thenThrow(
            DataIntegrityViolationException.class);

        assertThrows(
            DataIntegrityViolationException.class,
            () -> userRepository.save(duplicateUser),
            "Expected save to throw DataIntegrityViolationException, but it didn't"
        );

    }

    @Test
    void shouldFailValidationWhenMinAgeIsFalse() {
        UserRequestDTO userRequestDTO = new UserRequestDTO(
            "john", "john@email.com",
            "John", "Doe",
            LocalDate.of(2015, 1, 15),
            "aws.12bs.bucket.com", "user!123"
        );

        Set<ConstraintViolation<UserRequestDTO>> violations =
            validator.validate(userRequestDTO);

        assertFalse(violations.isEmpty(), "Expected validation errors");
        assertEquals(1, violations.size());
        assertEquals("Usuário tem que ter pelo menos 13 anos",
            violations.iterator().next().getMessage());
    }

    // b

    @Test
    void updateUserNullId() {
        UserUpdateRequestDTO userRequestDTO = new UserUpdateRequestDTO(
            "john@email.com",
            "John", "Doe",
            LocalDate.of(1990, 1, 15),
            "aws.12bs.bucket.com"
        );

        assertThrows(IllegalArgumentException.class,
            () -> userService.updateUser(null, userRequestDTO, loggedUserKeycloakId),
            "ID não pode ser nulo");
    }

    @Test
    void updateUserInvalidId() {
        UserUpdateRequestDTO userRequestDTO = new UserUpdateRequestDTO(
            "john@email.com",
            "John", "Doe",
            LocalDate.of(1990, 1, 15),
            "aws.12bs.bucket.com"
        );

        assertThrows(IllegalArgumentException.class,
            () -> userService.updateUser(-1L, userRequestDTO, loggedUserKeycloakId),
            "ID não pode ser negativo");
    }

    // c

    @Test
    void getUserByIdTrue() {
        User user = new User(
            "1abc23",
            "john",
            "John",
            "Doe",
            "john@email.com",
            LocalDate.of(1990, 1, 15),
            "www.foto.url",
            new ArrayList<>()
        );
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        UserResponseDTO userResponseDTO = new UserResponseDTO(user.getId(), user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getUsername(),
            user.getKeycloakId(),
            user.getEmail(),
            user.getFirstName(), user.getLastName(),
            user.getDateOfBirth(), user.getPhotoUrl());
        when(userMapper.toResponseDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO userById = userService.getUserById(1L);

        assertEquals(user.getId(), userById.id());
        assertNotNull(userById);
    }

    @Test
    void getUserByIdNullId() {
        assertThrows(IllegalArgumentException.class,
            () -> userService.getUserById(null),
            "ID não pode ser nulo");
    }

    @Test
    void getUserByIdFalseInvalidId() {
        assertThrows(IllegalArgumentException.class,
            () -> userService.getUserById(-1L),
            "ID não pode ser negativo");
    }

    // d

    @Test
    void deleteUserTrue() {
        User existingUser = new User("1abc23", "john",
            "John", "Doe", "john@email.com",
            LocalDate.of(1990, 1, 15),
            "www.foto.url", new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        userService.deleteUser(1L, loggedUserKeycloakId);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUserWithIdNull() {
        assertThrows(IllegalArgumentException.class,
            () -> userService.deleteUser(null, loggedUserKeycloakId),
            "ID não pode ser nulo");
    }

    @Test
    void deleteUserWithInvalidId() {
        assertThrows(IllegalArgumentException.class,
            () -> userService.deleteUser(-1L, loggedUserKeycloakId),
            "ID não pode ser negativo");
    }
    // Structure Tests

    @Test
    void succesfullyCreateUser() {

        User user = new User(
            "1abc23",
            "john",
            "John",
            "Doe",
            "john@email.com",
            LocalDate.of(1990, 1, 15),
            "www.foto.url",
            new ArrayList<>());

        UserRequestDTO userRequestDTO = new UserRequestDTO(
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getDateOfBirth(),
            user.getPhotoUrl(),
            "abc123"
        );

        UserResponseDTO userDto = new UserResponseDTO(
            user.getId(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getUsername(),
            user.getKeycloakId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getDateOfBirth(),
            user.getPhotoUrl()
        );

        KcUserResponseDTO kcUserResponseDTO = new KcUserResponseDTO(
            user.getKeycloakId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName()
        );

        when(userMapper.toEntity(userRequestDTO)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(userDto);
        when(keycloakServiceImpl.createUser(userRequestDTO)).thenReturn(kcUserResponseDTO);

        UserResponseDTO response = userService.createUser(userRequestDTO);

        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toEntity(userRequestDTO);
        verify(userMapper, times(1)).toResponseDTO(user);

        assertEquals(response.id(), user.getId());
        assertEquals(response.keycloakId(), user.getKeycloakId());
        assertEquals(response.username(), user.getUsername());
        assertEquals(response.firstName(), user.getFirstName());

        assertEquals(kcUserResponseDTO.id(), response.keycloakId());
        assertEquals(kcUserResponseDTO.email(), user.getEmail());
        assertEquals(kcUserResponseDTO.firstName(), user.getFirstName());
        assertEquals(kcUserResponseDTO.lastName(), user.getLastName());
        assertEquals(kcUserResponseDTO.username(), user.getUsername());
    }


    @Test
    void getPetsUserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
            () -> userService.getPets("123"),
            "Usuário não encontrado");
    }

    @Test
    void succesfullyGetPets() {
        User user = new User(
            "1abc23",
            "john",
            "John",
            "Doe",
            "john@email.com",
            LocalDate.of(1990, 1, 15), "www.foto.url",
            new ArrayList<>()
        );

        Pet pet = new Pet();
        pet.setId(1L);
        pet.setName("Apolo");
        pet.setSpecies(Species.DOG);
        pet.setHeight(30);
        pet.setWeight(BigDecimal.valueOf(10.0));

        Pet pet2 = new Pet();
        pet.setId(2L);
        pet.setName("Mike");
        pet.setSpecies(Species.CAT);
        pet.setHeight(20);
        pet.setWeight(BigDecimal.valueOf(5.0));

        user.getPets().add(pet);
        user.getPets().add(pet2);

        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        PetResponseDTO petResponse1 = new PetResponseDTO(
            pet.getId(),
            pet.getCreatedAt(),
            pet.getUpdatedAt(),
            pet.getName(),
            pet.getGender(),
            pet.getBirthdate(),
            pet.getWeight(),
            pet.getBreed(),
            pet.getSpecies(),
            pet.getHeight(),
            pet.getPhotoUrl()
        );

        PetResponseDTO petResponse2 = new PetResponseDTO(
            pet2.getId(),
            pet2.getCreatedAt(),
            pet2.getUpdatedAt(),
            pet2.getName(),
            pet2.getGender(),
            pet2.getBirthdate(),
            pet2.getWeight(),
            pet2.getBreed(),
            pet2.getSpecies(),
            pet2.getHeight(),
            pet2.getPhotoUrl()
        );

        List<PetResponseDTO> petResponses = new ArrayList<>();
        petResponses.add(petResponse1);
        petResponses.add(petResponse2);

        when(petMapper.toDTOList(user.getPets())).thenReturn(petResponses);
        List<PetResponseDTO> response = userService.getPets("123");

        verify(userRepository, times(1)).findById(1L);
        verify(petMapper, times(1)).toDTOList(user.getPets());

        assertEquals(petResponses.getFirst().id(), response.getFirst().id());
        assertEquals(petResponses.getFirst().name(), response.getFirst().name());
        assertEquals(petResponses.getFirst().species(), response.getFirst().species());
        assertEquals(petResponses.getFirst().height(), response.getFirst().height());
        assertEquals(petResponses.getFirst().weight(), response.getFirst().weight());

        assertEquals(petResponses.get(1).id(), response.get(1).id());
        assertEquals(petResponses.get(1).name(), response.get(1).name());
        assertEquals(petResponses.get(1).species(), response.get(1).species());
        assertEquals(petResponses.get(1).height(), response.get(1).height());
        assertEquals(petResponses.get(1).weight(), response.get(1).weight());

    }

    @Test
    void succesfullyUpdateUser() {
        // Arrange: Configuração do Usuário e DTOs
        User user = new User(
            "1abc23",
            "john",
            "John",
            "Doe",
            "john@email.com",
            LocalDate.of(1990, 1, 15),
            "www.foto.url",
            new ArrayList<>()
        );

        UserUpdateRequestDTO userRequestDTO = new UserUpdateRequestDTO(
            user.getEmail(),
            "jhon updated",
            "doe updated",
            user.getDateOfBirth(),
            "www.newphoto.url"
        );

        KcUserResponseDTO kcUserResponseDTO = new KcUserResponseDTO(
            user.getKeycloakId(),
            user.getUsername(),
            userRequestDTO.email(),
            userRequestDTO.firstName(),
            userRequestDTO.lastName()
        );

        UserResponseDTO userResponseDTO = new UserResponseDTO(
            user.getId(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            kcUserResponseDTO.username(),
            kcUserResponseDTO.id(),
            kcUserResponseDTO.email(),
            kcUserResponseDTO.firstName(),
            kcUserResponseDTO.lastName(),
            userRequestDTO.dateOfBirth(),
            userRequestDTO.photoUrl()
        );

        // Mock: Simulação dos Comportamentos
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(keycloakServiceImpl.updateUser(user.getKeycloakId(), userRequestDTO)).thenReturn(
            kcUserResponseDTO);
        doAnswer(invocation -> {
            // Atualiza o usuário local com os dados retornados do Keycloak
            user.setUsername(kcUserResponseDTO.username());
            user.setFirstName(kcUserResponseDTO.firstName());
            user.setLastName(kcUserResponseDTO.lastName());
            user.setEmail(kcUserResponseDTO.email());
            user.setDateOfBirth(userRequestDTO.dateOfBirth());
            user.setPhotoUrl(userRequestDTO.photoUrl());
            return null;
        }).when(userMapper).updateEntityFromDTO(userRequestDTO, user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(userResponseDTO);

        // Act: Chamada do Método a Ser Testado
        UserResponseDTO response = userService.updateUser(
            1L,
            userRequestDTO,
            loggedUserKeycloakId
        );

        // Assert: Verificação dos Resultados
        verify(userRepository, times(1)).findById(1L);
        verify(keycloakServiceImpl, times(1))
            .updateUser(user.getKeycloakId(), userRequestDTO);
        verify(userMapper, times(1)).
            updateEntityFromDTO(userRequestDTO, user);
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toResponseDTO(user);

        // Validação dos Dados Atualizados
        assertEquals(userResponseDTO.id(), response.id());
        assertEquals(userResponseDTO.username(), response.username());
        assertEquals(userResponseDTO.firstName(), response.firstName());
        assertEquals(userResponseDTO.lastName(), response.lastName());
        assertEquals(userResponseDTO.email(), response.email());
        assertEquals(userResponseDTO.dateOfBirth(), response.dateOfBirth());
        assertEquals(userResponseDTO.photoUrl(), response.photoUrl());
    }


}
