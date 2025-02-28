package br.com.ifrn.ddldevs.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Species;
import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
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
import org.springframework.cglib.core.Local;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

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

    @Mock
    private UploadImageService uploadImageService;

    private Validator validator;

    private final String loggedUserKeycloakId = "1abc23";

    private final MultipartFile mockImage = new MockMultipartFile(
        "photoUrl",
        "image.jpg",
        "image/jpeg",
        "content".getBytes()
    );

    @InjectMocks
    UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "jhon",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_user"))
        );
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // a

    @Test
    void shouldPassValidationForValidUserRequestDTO() {
        UserRequestDTO userRequestDTO = new UserRequestDTO(
            "john", "john@email.com",
            "John", "Doe",
            LocalDate.of(1990, 1, 15),
            mockImage, "user!123"
        );

        KcUserResponseDTO kcResponseDTO = new KcUserResponseDTO(
                "345",
                "john",
                "john@email.com",
                "Jhon",
                "Doe"
        );

        User user = new User(
                kcResponseDTO.id(),
                userRequestDTO.username(),
                userRequestDTO.firstName(),
                userRequestDTO.lastName(),
                userRequestDTO.email(),
                userRequestDTO.dateOfBirth(),
                "www.photo.com",
                new ArrayList<Pet>()
        );

        UserResponseDTO userResponseDTO = new UserResponseDTO(user.getId(), user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getUsername(),
                user.getKeycloakId(),
                user.getEmail(),
                user.getFirstName(), user.getLastName(),
                user.getDateOfBirth(), user.getPhotoUrl());

        when(userMapper.toResponseDTO(user)).thenReturn(userResponseDTO);
        when(keycloakServiceImpl.createUser(any())).thenReturn(kcResponseDTO);
        when(userMapper.toEntity(userRequestDTO)).thenReturn(user);
        when(uploadImageService.uploadImg(any())).thenReturn("www.photo.com");
        when(userRepository.save(user)).thenReturn(user);

        UserResponseDTO response = userService.createUser(userRequestDTO);

        assertNotNull(response);
        verify(keycloakServiceImpl).createUser(userRequestDTO);
        verify(userMapper).toEntity(userRequestDTO);
        verify(uploadImageService).uploadImg(any());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toEntity(userRequestDTO);

        assertEquals(response.id(), user.getId());
        assertEquals(response.username(), user.getUsername());
    }

    @Test
    void shouldFailValidationWhenFieldsAreNull() {
        UserRequestDTO userRequestDTO = new UserRequestDTO(
            "john",
            null, null, null,
            LocalDate.of(1990, 1, 15),
            mockImage, null
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
            mockImage, "user!123"
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
    void shouldFailValidationWhenMinAgePasswordIsFalse() {
        UserRequestDTO userRequestDTO = new UserRequestDTO(
            "john", "john@email.com",
            "John", "Doe",
            LocalDate.of(2015, 1, 15),
            mockImage, "user"
        );

        Set<ConstraintViolation<UserRequestDTO>> violations =
            validator.validate(userRequestDTO);

        assertFalse(violations.isEmpty(), "Expected validation errors");
        assertEquals(2, violations.size());
    }

    // b
    @Test
    void shouldSuccessfullyUpdateUser() {
        User existingUser = new User("1abc23", "john",
                "John", "Doe", "john@email.com",
                LocalDate.of(1990, 1, 15),
                "www.foto.url", new ArrayList<>());

        UserUpdateRequestDTO userRequestDTO = new UserUpdateRequestDTO(
                "updated_jhon@gmail.com",
                "updated_jhon",
                "updated_doe",
                LocalDate.of(2024, 10, 5),
                mockImage
        );

        KcUserResponseDTO kcResponseDTO = new KcUserResponseDTO(
                existingUser.getKeycloakId(),
                existingUser.getUsername(),
                userRequestDTO.email(),
                userRequestDTO.firstName(),
                userRequestDTO.lastName()
        );

        User updatedUser = new User(
                kcResponseDTO.id(),
                existingUser.getUsername(),
                userRequestDTO.firstName(),
                userRequestDTO.lastName(),
                userRequestDTO.email(),
                userRequestDTO.dateOfBirth(),
                "www.new_photo.com",
                new ArrayList<Pet>()
        );

        UserResponseDTO userResponseDTO = new UserResponseDTO(updatedUser.getId(), updatedUser.getCreatedAt(),
                updatedUser.getUpdatedAt(),
                updatedUser.getUsername(),
                updatedUser.getKeycloakId(),
                updatedUser.getEmail(),
                updatedUser.getFirstName(), updatedUser.getLastName(),
                updatedUser.getDateOfBirth(), updatedUser.getPhotoUrl());

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(keycloakServiceImpl.updateUser(existingUser.getKeycloakId(), userRequestDTO)).thenReturn(kcResponseDTO);
        doNothing().when(userMapper).updateEntityFromDTO(userRequestDTO, existingUser);
        when(uploadImageService.uploadImg(any())).thenReturn("www.new_photo.com");
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        when(userMapper.toResponseDTO(updatedUser)).thenReturn(userResponseDTO);

        UserResponseDTO response = userService.updateUser(1L, userRequestDTO, loggedUserKeycloakId);

        assertNotNull(response);
        verify(keycloakServiceImpl).updateUser(existingUser.getKeycloakId(), userRequestDTO);
        verify(userMapper).toResponseDTO(updatedUser);
        verify(uploadImageService).uploadImg(any());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponseDTO(updatedUser);
        verify(userMapper).updateEntityFromDTO(userRequestDTO, existingUser);

        assertEquals(response.id(), updatedUser.getId());
        assertEquals(response.email(), updatedUser.getEmail());
    }

    @Test
    void updateUserNullId() {
        UserUpdateRequestDTO userRequestDTO = new UserUpdateRequestDTO(
            "john@email.com",
            "John", "Doe",
            LocalDate.of(1990, 1, 15),
            mockImage
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
            mockImage
        );

        assertThrows(IllegalArgumentException.class,
            () -> userService.updateUser(-1L, userRequestDTO, loggedUserKeycloakId),
            "ID não pode ser negativo");
    }

    @Test
    void shouldFailUpdateWhenInvalidInfo() {
        UserUpdateRequestDTO userRequestDTO = new UserUpdateRequestDTO(
                "updated_jhongmail.com",
                "",
                "",
                LocalDate.of(2024, 10, 5),
                mockImage
        );

        Set<ConstraintViolation<UserUpdateRequestDTO>> violations =
                validator.validate(userRequestDTO);

        assertFalse(violations.isEmpty(), "Expected validation errors");
        assertEquals(4, violations.size(), "Expected exactly one validation error");
    }

    @Test
    void shouldFailUpdateWhenEmailEmpty() {
        UserUpdateRequestDTO userRequestDTO = new UserUpdateRequestDTO(
                "",
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<UserUpdateRequestDTO>> violations =
                validator.validate(userRequestDTO);

        for (ConstraintViolation<UserUpdateRequestDTO> violation : violations) {
            System.out.println(violation.getPropertyPath() + violation.getMessage());
        }
        assertFalse(violations.isEmpty(), "Expected validation errors");
        assertEquals(1, violations.size(), "Expected exactly one validation error");
    }

    @Test
    void shouldFailUpdateWhenEmailExists() {

        User existingUser = new User("1abc23", "john",
                "John", "Doe", "a_john@email.com",
                LocalDate.of(1990, 1, 15),
                "www.foto.url", new ArrayList<>());

        UserUpdateRequestDTO userRequestDTO = new UserUpdateRequestDTO(
                "john@email.com",
                "updated_jhon",
                "updated_doe",
                LocalDate.of(2024, 10, 5),
                null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(keycloakServiceImpl.updateUser(existingUser.getKeycloakId(), userRequestDTO)).thenReturn(any());

        when(userRepository.save(existingUser)).thenThrow(
                DataIntegrityViolationException.class);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userService.updateUser(1L, userRequestDTO, loggedUserKeycloakId),
                "Expected save to throw DataIntegrityViolationException, but it didn't"
        );
    }

    @Test
    void shouldNotUpdateWhenIsNotAccountOwner() {
        User user = new User("1abc23", "john",
                "John", "Doe", "a_john@email.com",
                LocalDate.of(1990, 1, 15),
                "www.foto.url", new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                AccessDeniedException.class,
                () -> userService.updateUser(1L, any(), "NotOwner")
        );
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

    @Test
    void shouldFailWhenNotFoundUser(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(1L)
        );
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

    @Test
    void shouldNotDeleteUserWhenNotOwner() {
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

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                AccessDeniedException.class,
                () -> userService.deleteUser(1L, "NotOwner")
        );
    }

    @Test
    void deleteNotFound(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.deleteUser(1L, any())
        );
    }

    // e

    @Test
    void shouldFailWhenNotFoundMe(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getCurrentUser("NotFound")
        );
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
            mockImage,
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

        assertThrows(ResourceNotFoundException.class,
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

        when(userRepository.findByKeycloakId(any())).thenReturn(Optional.of(user));

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
        List<PetResponseDTO> response = userService.getPets("1abc23");

        verify(userRepository, times(1)).findByKeycloakId(any());
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
    void successfullyGetUsers(){
        User user = new User(
                "1abc23",
                "john",
                "John",
                "Doe",
                "john@email.com",
                LocalDate.of(1990, 1, 15), "www.foto.url",
                new ArrayList<>()
        );
        User user2 = new User(
                "1abc231",
                "john1",
                "John1",
                "Doe1",
                "john1@email.com",
                LocalDate.of(1990, 1, 15), "www.foto1.url",
                new ArrayList<>()
        );

        UserResponseDTO userResponse1 = new UserResponseDTO(
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

        UserResponseDTO userResponse2 = new UserResponseDTO(
                user2.getId(),
                user2.getCreatedAt(),
                user2.getUpdatedAt(),
                user2.getUsername(),
                user2.getKeycloakId(),
                user2.getEmail(),
                user2.getFirstName(),
                user2.getLastName(),
                user2.getDateOfBirth(),
                user2.getPhotoUrl()
        );

        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(user2);

        List<UserResponseDTO> userResponses = new ArrayList<>();
        userResponses.add(userResponse1);
        userResponses.add(userResponse2);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDTOList(users)).thenReturn(userResponses);

        List<UserResponseDTO> responses = userService.listUsers();

        verify(userMapper, times(1)).toDTOList(users);
        verify(userRepository, times(1)).findAll();

        assertEquals(users.getFirst().getId(), responses.getFirst().id());
        assertEquals(users.getFirst().getUsername(), responses.getFirst().username());

        assertEquals(users.get(1).getId(), userResponses.get(1).id());
        assertEquals(users.get(1).getUsername(), userResponses.get(1).username());
    }

}
