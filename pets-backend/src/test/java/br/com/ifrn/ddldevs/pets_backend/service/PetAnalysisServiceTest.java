package br.com.ifrn.ddldevs.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Species;
import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import br.com.ifrn.ddldevs.pets_backend.domain.PetAnalysis;
import br.com.ifrn.ddldevs.pets_backend.domain.User;
import br.com.ifrn.ddldevs.pets_backend.dto.PetAnalysis.PetAnalysisRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.PetAnalysis.PetAnalysisResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.mapper.PetAnalysisMapper;
import br.com.ifrn.ddldevs.pets_backend.repository.PetAnalysisRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.PetRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PetAnalysisServiceTest {

    @Mock
    private PetAnalysisRepository petAnalysisRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private PetAnalysisMapper petAnalysisMapper;

    @InjectMocks
    private PetAnalysisService petAnalysisService;

    private final String loggedUserKeycloakId = "1abc23";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPetAnalysisWithValidPet() {
        User user = new User();
        user.setId(1L);
        user.setUsername("jhon");
        user.setFirstName("Jhon");
        user.setEmail("jhon@gmail.com");
        user.setKeycloakId(loggedUserKeycloakId);

        Pet pet = new Pet();
        pet.setId(1L);
        pet.setName("Apolo");
        pet.setSpecies(Species.DOG);
        pet.setHeight(30);
        pet.setWeight(BigDecimal.valueOf(10.0));
        pet.setUser(user);

        PetAnalysisRequestDTO requestDTO = new PetAnalysisRequestDTO(1L,
            "http://example.com/picture.jpg", "Healthy", "Blood Test");
        PetAnalysis petAnalysis = new PetAnalysis();
        PetAnalysisResponseDTO responseDTO = new PetAnalysisResponseDTO(
            1L,
            LocalDateTime.now(),
            LocalDateTime.now(),
            "http://example.com/picture.jpg",
            "Healthy",
            "Blood Test"
        );

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petAnalysisMapper.toEntity(requestDTO)).thenReturn(petAnalysis);
        when(petAnalysisRepository.save(petAnalysis)).thenReturn(petAnalysis);
        when(petAnalysisMapper.toResponse(petAnalysis)).thenReturn(responseDTO);

        PetAnalysisResponseDTO result = petAnalysisService.createPetAnalysis(requestDTO,
            loggedUserKeycloakId);

        assertNotNull(result);
        assertEquals("http://example.com/picture.jpg", result.picture());
        assertEquals("Healthy", result.result());
        assertEquals("Blood Test", result.analysisType());

        verify(petRepository).findById(1L);
        verify(petAnalysisRepository).save(petAnalysis);
    }

    @Test
    void createPetAnalysisWithInvalidPet() {
        PetAnalysisRequestDTO requestDTO = new PetAnalysisRequestDTO(-1L,
            "http://example.com/picture.jpg", "Healthy", "Blood Test");

        when(petRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> petAnalysisService.createPetAnalysis(requestDTO, loggedUserKeycloakId));

        assertEquals("ID não pode ser negativo", exception.getMessage());

        verify(petAnalysisRepository, never()).save(any(PetAnalysis.class));
    }

    @Test
    void createPetAnalysisWithNullPet() {
        PetAnalysisRequestDTO requestDTO = new PetAnalysisRequestDTO(-1L,
            "http://example.com/picture.jpg", "Healthy", "Blood Test");

        assertThrows(IllegalArgumentException.class,
            () -> petAnalysisService.createPetAnalysis(requestDTO, loggedUserKeycloakId),
            "ID não pode ser nulo");
    }

    // b

    @Test
    void deletePetAnalysisWithValidId() {
        User user = new User();
        user.setId(1L);
        user.setUsername("jhon");
        user.setFirstName("Jhon");
        user.setEmail("jhon@gmail.com");
        user.setKeycloakId(loggedUserKeycloakId);

        Pet pet = new Pet();
        pet.setId(2L);
        pet.setName("Apolo");
        pet.setSpecies(Species.DOG);
        pet.setHeight(30);
        pet.setWeight(BigDecimal.valueOf(10.0));
        pet.setUser(user);

        PetAnalysis petAnalysis = new PetAnalysis();
        petAnalysis.setId(1L);
        petAnalysis.setPet(pet);
        petAnalysis.setPicture("http://example.com/picture.jpg");
        petAnalysis.setResult("Healthy");
        petAnalysis.setAnalysisType("Emotion Test");

        when(petAnalysisRepository.findById(1L)).thenReturn(Optional.of(petAnalysis));
        when(petAnalysisRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> petAnalysisService.deletePetAnalysis(1L, loggedUserKeycloakId));

        verify(petAnalysisRepository).deleteById(1L);
    }

    @Test
    void deletePetAnalysisWithIdNull() {
        assertThrows(IllegalArgumentException.class,
            () -> petAnalysisService.deletePetAnalysis(null, loggedUserKeycloakId),
            "ID não pode ser nulo");
    }

    @Test
    void deletePetWithInvalidId() {
        assertThrows(IllegalArgumentException.class,
            () -> petAnalysisService.deletePetAnalysis(-1L, loggedUserKeycloakId),
            "ID não pode ser negativo");
    }

    // d

    @Test
    void getPetAnalysesByPetIdWithValidId() {
        List<PetAnalysis> analyses = new ArrayList<>();

        User user = new User();
        user.setId(1L);
        user.setUsername("jhon");
        user.setFirstName("Jhon");
        user.setEmail("jhon@gmail.com");
        user.setKeycloakId(loggedUserKeycloakId);

        Pet pet = new Pet();
        pet.setId(1L);
        pet.setName("Apolo");
        pet.setSpecies(Species.DOG);
        pet.setHeight(30);
        pet.setWeight(BigDecimal.valueOf(10.0));
        pet.setUser(user);

        PetAnalysis analyse = new PetAnalysis();
        analyse.setId(1L);
        analyse.setPet(pet);
        analyse.setAnalysisType("Blood Test");
        analyse.setResult("Healthy");
        analyse.setPicture("http://example.com/picture.jpg");

        analyses.add(analyse);

        when(petAnalysisRepository.findAllByPetId(1L)).thenReturn(analyses);
        when(petAnalysisMapper.toResponseList(analyses)).thenReturn(new ArrayList<>());

        List<PetAnalysisResponseDTO> response = petAnalysisService.getAllByPetId(1L,
            loggedUserKeycloakId);

        assertNotNull(response);
        verify(petAnalysisRepository).findAllByPetId(1L);
    }

    @Test
    void getPetAnalysesByPetIdWithInvalidId() {
        assertThrows(IllegalArgumentException.class,
            () -> petAnalysisService.getAllByPetId(-1L, loggedUserKeycloakId),
            "ID não pode ser negativo");
    }

    @Test
    void getPetAnalysesByPetIdWithNullId() {
        assertThrows(IllegalArgumentException.class,
            () -> petAnalysisService.getAllByPetId(null, loggedUserKeycloakId),
            "ID não pode ser nulo");
    }

    // e

    @Test
    void getPetAnalysesWithValidId() {
        User user = new User();
        user.setId(1L);
        user.setUsername("jhon");
        user.setFirstName("Jhon");
        user.setEmail("jhon@gmail.com");
        user.setKeycloakId(loggedUserKeycloakId);

        Pet pet = new Pet();
        pet.setId(1L);
        pet.setName("Apolo");
        pet.setSpecies(Species.DOG);
        pet.setHeight(30);
        pet.setWeight(BigDecimal.valueOf(10.0));
        pet.setUser(user);

        PetAnalysis analyses = new PetAnalysis();
        analyses.setId(1L);
        analyses.setPet(pet);
        analyses.setAnalysisType("Blood Test");
        analyses.setResult("Healthy");
        analyses.setPicture("http://example.com/picture.jpg");

        PetAnalysisResponseDTO responseDTO = new PetAnalysisResponseDTO(
            1L,
            LocalDateTime.now(),
            LocalDateTime.now(),
            "http://example.com/picture.jpg",
            "Healthy",
            "Blood Test"
        );

        when(petAnalysisRepository.findById(1L)).thenReturn(Optional.of(analyses));
        when(petAnalysisMapper.toResponse(analyses)).thenReturn(responseDTO);

        PetAnalysisResponseDTO result = petAnalysisService.getPetAnalysis(
            1L,
            loggedUserKeycloakId
        );

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void getPetAnalysesWithInvalidId() {
        assertThrows(IllegalArgumentException.class,
            () -> petAnalysisService.getPetAnalysis(-1L, loggedUserKeycloakId),
            "ID não pode ser negativo");
    }

    @Test
    void getPetAnalysesIdWithNullId() {
        assertThrows(IllegalArgumentException.class,
            () -> petAnalysisService.getAllByPetId(null, loggedUserKeycloakId),
            "ID não pode ser nulo");
    }
}