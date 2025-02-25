package br.com.ifrn.ddldevs.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Species;
import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import br.com.ifrn.ddldevs.pets_backend.domain.User;
import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetUpdateRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.exception.ResourceNotFoundException;
import br.com.ifrn.ddldevs.pets_backend.mapper.PetMapper;
import br.com.ifrn.ddldevs.pets_backend.repository.PetRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private PetMapper petMapper;

    @InjectMocks
    private PetService petService;

    private Validator validator;

    private final String loggedUserKeycloakId = "1abc23";

    public PetServiceTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPetValidInformation() {
        User user = new User();
        user.setId(1L);
        user.setUsername("jhon");
        user.setFirstName("Jhon");
        user.setEmail("jhon@gmail.com");
        user.setKeycloakId(loggedUserKeycloakId);
        user.setPets(new ArrayList<>());

        when(userRepository.findByKeycloakId(loggedUserKeycloakId)).thenReturn(Optional.of(user));

        PetRequestDTO dto = new PetRequestDTO();
        dto.setName("Apolo");
        dto.setSpecies(Species.DOG);
        dto.setHeight(30);
        dto.setWeight(BigDecimal.valueOf(10.0));

        Pet pet = new Pet();
        pet.setId(1L);
        pet.setName(dto.getName());
        pet.setSpecies(dto.getSpecies());
        pet.setHeight(dto.getHeight());
        pet.setWeight(dto.getWeight());
        pet.setUser(user);

        PetResponseDTO petResponse = new PetResponseDTO(
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

        when(petMapper.toEntity(dto)).thenReturn(pet);
        when(petRepository.save(any(Pet.class))).thenReturn(pet);
        when(petMapper.toPetResponseDTO(pet)).thenReturn(petResponse);

        PetResponseDTO response = petService.createPet(dto, loggedUserKeycloakId);

        assertNotNull(response);
        assertEquals("Apolo", response.name());
        assertEquals(Species.DOG, response.species());
        assertEquals(30, response.height());
        assertEquals(BigDecimal.valueOf(10.0), response.weight());

        verify(userRepository).findByKeycloakId(loggedUserKeycloakId);
        verify(petMapper).toEntity(dto);
        verify(petRepository).save(any(Pet.class));
        verify(petMapper).toPetResponseDTO(pet);
    }

    @Test
    void createPetInvalidInformation() {
        PetRequestDTO dto = new PetRequestDTO();
        dto.setName("");
        dto.setWeight(BigDecimal.valueOf(-5.0));
        dto.setSpecies(Species.DOG);
        dto.setBreed("");
        dto.setHeight(0);

        Set<ConstraintViolation<PetRequestDTO>> violations = validator.validate(dto);
        System.out.println(violations);
        assertFalse(violations.isEmpty());
        assertEquals(4, violations.size());
    }

    @Test
    void createWithInvalidGenderType() {
        ObjectMapper objectMapper = new ObjectMapper();
        String invalidJson = """
                {
                    "name": "Buddy",
                    "gender": "INVALID",
                    "weight": 10.0,
                    "species": "Dog",
                    "breed": "Golden Retriever",
                    "height": 50,
                    "userId": 1
                }
            """;

        InvalidFormatException exception = assertThrows(
            InvalidFormatException.class,
            () -> objectMapper.readValue(invalidJson, PetRequestDTO.class)
        );
        assertTrue(exception.getMessage()
            .contains("not one of the values accepted for Enum class: [FEMALE, MALE]"));
    }

    // b

    @Test
    void updatePetSuccessful() {
        Long petId = 1L;

        User user = new User();
        user.setId(1L);
        user.setUsername("jhon");
        user.setFirstName("Jhon");
        user.setEmail("jhon@gmail.com");
        user.setKeycloakId(loggedUserKeycloakId);

        Pet existingPet = new Pet();
        existingPet.setId(petId);
        existingPet.setName("Buddy");
        existingPet.setSpecies(Species.CAT);
        existingPet.setHeight(20);
        existingPet.setWeight(BigDecimal.valueOf(5.0));
        existingPet.setUser(user);

        PetUpdateRequestDTO updatedDTO = new PetUpdateRequestDTO();
        updatedDTO.setName("Apolo");
        updatedDTO.setSpecies(Species.DOG);
        updatedDTO.setHeight(30);
        updatedDTO.setWeight(BigDecimal.valueOf(10.0));

        Pet updatedPet = new Pet();
        updatedPet.setId(petId);
        updatedPet.setName("Apolo");
        updatedPet.setSpecies(Species.DOG);
        updatedPet.setHeight(30);
        updatedPet.setWeight(BigDecimal.valueOf(10.0));

        PetResponseDTO expectedResponse = new PetResponseDTO(
            petId,
            LocalDateTime.now(),
            LocalDateTime.now(),
            "Apolo",
            null,
            null,
            BigDecimal.valueOf(10.0),
            null,
            Species.DOG,
            30,
            null
        );

        when(petRepository.findById(petId)).thenReturn(Optional.of(existingPet));
        doAnswer(invocation -> {
            PetUpdateRequestDTO dto = invocation.getArgument(0);
            Pet pet = invocation.getArgument(1);
            pet.setName(dto.getName());
            pet.setSpecies(dto.getSpecies());
            pet.setHeight(dto.getHeight());
            pet.setWeight(dto.getWeight());
            return null;
        }).when(petMapper).updateEntityFromDTO(updatedDTO, existingPet);
        when(petRepository.save(existingPet)).thenReturn(updatedPet);
        when(petMapper.toPetResponseDTO(updatedPet)).thenReturn(expectedResponse);

        PetResponseDTO response = petService.updatePet(petId, updatedDTO, loggedUserKeycloakId);

        assertNotNull(response);
        assertEquals(expectedResponse.id(), response.id());
        assertEquals(expectedResponse.name(), response.name());
        assertEquals(expectedResponse.species(), response.species());
        assertEquals(expectedResponse.height(), response.height());
        assertEquals(expectedResponse.weight(), response.weight());

        verify(petRepository).findById(petId);
        verify(petMapper).updateEntityFromDTO(updatedDTO, existingPet);
        verify(petRepository).save(existingPet);
        verify(petMapper).toPetResponseDTO(updatedPet);
    }

    @Test
    void updatePetNullId() {
        PetUpdateRequestDTO updatedPetDto = new PetUpdateRequestDTO();
        updatedPetDto.setName("Apolo");
        updatedPetDto.setSpecies(Species.DOG);
        updatedPetDto.setHeight(30);
        updatedPetDto.setWeight(BigDecimal.valueOf(10.0));

        assertThrows(IllegalArgumentException.class,
            () -> petService.updatePet(null, updatedPetDto, loggedUserKeycloakId),
            "ID não pode ser nulo");
    }

    @Test
    void updatePetInvalidId() {
        PetUpdateRequestDTO updatedPetDto = new PetUpdateRequestDTO();
        updatedPetDto.setName("Apolo");
        updatedPetDto.setSpecies(Species.DOG);
        updatedPetDto.setHeight(30);
        updatedPetDto.setWeight(BigDecimal.valueOf(10.0));

        assertThrows(IllegalArgumentException.class,
            () -> petService.updatePet(-1L, updatedPetDto, loggedUserKeycloakId),
            "ID não pode ser nulo");
    }

    // c

    @Test
    void getPetWithValidId() {
        User user = new User();
        user.setId(1L);
        user.setUsername("jhon");
        user.setFirstName("Jhon");
        user.setEmail("jhon@gmail.com");
        user.setKeycloakId(loggedUserKeycloakId);

        Long validId = 1L;
        Pet pet = new Pet();
        pet.setId(validId);
        pet.setName("Apolo");
        pet.setSpecies(Species.DOG);
        pet.setHeight(30);
        pet.setWeight(BigDecimal.valueOf(10.0));
        pet.setUser(user);

        PetResponseDTO petResponseDTO = new PetResponseDTO(
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

        when(petRepository.findById(validId)).thenReturn(Optional.of(pet));
        when(petMapper.toPetResponseDTO(pet)).thenReturn(petResponseDTO);

        PetResponseDTO response = petService.getPet(validId, loggedUserKeycloakId);

        assertNotNull(response);
        assertEquals(validId, response.id());
        assertEquals("Apolo", response.name());
        assertEquals(Species.DOG, response.species());
        assertEquals(30, response.height());
        assertEquals(BigDecimal.valueOf(10.0), response.weight());
    }

    @Test
    void getPetByIdNullId() {
        assertThrows(IllegalArgumentException.class,
            () -> petService.getPet(null, loggedUserKeycloakId),
            "ID não pode ser nulo");
    }

    @Test
    void getPetByIdFalseInvalidId() {
        assertThrows(IllegalArgumentException.class,
            () -> petService.getPet(-1L, loggedUserKeycloakId),
            "ID não pode ser negativo");
    }

    // d

    @Test
    void deletePetWithValidId() {
        Long validId = 1L;

        User user = new User();
        user.setId(1L);
        user.setUsername("jhon");
        user.setFirstName("Jhon");
        user.setEmail("jhon@gmail.com");
        user.setKeycloakId(loggedUserKeycloakId);

        Pet pet = new Pet();
        pet.setId(validId);
        pet.setName("Apolo");
        pet.setSpecies(Species.DOG);
        pet.setHeight(30);
        pet.setWeight(BigDecimal.valueOf(10.0));
        pet.setUser(user);

        when(petRepository.findById(validId)).thenReturn(Optional.of(pet));
        when(petRepository.existsById(validId)).thenReturn(true);

        assertDoesNotThrow(() -> petService.deletePet(validId, loggedUserKeycloakId));

        verify(petRepository).deleteById(validId);
    }

    @Test
    void deletePetWithIdNull() {
        assertThrows(IllegalArgumentException.class,
            () -> petService.deletePet(null, loggedUserKeycloakId),
            "ID não pode ser nulo");
    }

    @Test
    void deletePetWithInvalidId() {
        assertThrows(IllegalArgumentException.class,
            () -> petService.deletePet(-1L, loggedUserKeycloakId),
            "ID não pode ser negativo");
    }

    // Structure Tests

    @Test
    void updateNotFoundPet() {
        PetUpdateRequestDTO updatedPetDto = new PetUpdateRequestDTO();
        updatedPetDto.setName("Apolo");
        updatedPetDto.setSpecies(Species.DOG);
        updatedPetDto.setHeight(30);
        updatedPetDto.setWeight(BigDecimal.valueOf(10.0));

        when(petRepository.findById(30L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> petService.updatePet(30L, updatedPetDto, loggedUserKeycloakId),
            "Pet não encontrado");
    }

    @Test
    void deleteNotFoundPet() {

        when(petRepository.existsById(30L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
            () -> petService.deletePet(30L, loggedUserKeycloakId),
            "Pet não encontrado");
    }

    @Test
    void getNotFoundPet() {
        when(petRepository.findById(30L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> petService.getPet(30L, loggedUserKeycloakId),
            "Pet não encontrado");
    }
}