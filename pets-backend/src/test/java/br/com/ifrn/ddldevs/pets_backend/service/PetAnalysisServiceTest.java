package br.com.ifrn.ddldevs.pets_backend.service;

import br.com.ifrn.ddldevs.pets_backend.amazonSqs.AnalysisMessage;
import br.com.ifrn.ddldevs.pets_backend.amazonSqs.SQSSenderService;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisStatus;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Species;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import br.com.ifrn.ddldevs.pets_backend.domain.PetAnalysis;
import br.com.ifrn.ddldevs.pets_backend.domain.Recommendation;
import br.com.ifrn.ddldevs.pets_backend.domain.User;
import br.com.ifrn.ddldevs.pets_backend.dto.PetAnalysis.PetAnalysisRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.PetAnalysis.PetAnalysisResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.exception.AccessDeniedException;
import br.com.ifrn.ddldevs.pets_backend.mapper.PetAnalysisMapper;
import br.com.ifrn.ddldevs.pets_backend.repository.PetAnalysisRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.PetRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
class PetAnalysisServiceTest {

    @Mock
    private PetAnalysisRepository petAnalysisRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private PetAnalysisMapper petAnalysisMapper;

    @Mock
    private UploadImageService uploadImageService;

    @InjectMocks
    private PetAnalysisService petAnalysisService;

    private final String loggedUserKeycloakId = "1abc23";

    private Validator validator;

    @MockitoBean
    private SqsTemplate sqsTemplate;

    @MockitoBean
    private final MultipartFile mockImage = new MockMultipartFile(
        "photoUrl",
        "image.jpg",
        "image/jpeg",
        "content".getBytes()
    );

    @Mock
    private SQSSenderService sqsSenderService;

    public PetAnalysisServiceTest(){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "jhon",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_user"))
        );
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
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

        PetAnalysisRequestDTO requestDTO = new PetAnalysisRequestDTO(1L, mockImage, AnalysisType.BREED);
        PetAnalysis petAnalysis = new PetAnalysis();
        PetAnalysisResponseDTO responseDTO = new PetAnalysisResponseDTO(1L, LocalDateTime.now(),  LocalDateTime.now(),"http://example.com/picture.jpg", "Healthy", 83.24,AnalysisType.BREED, AnalysisStatus.COMPLETED);

        AnalysisMessage analysisMessage = new AnalysisMessage(1L, "http", "DOG_BREED");

        when(petAnalysisMapper.toEntity(requestDTO)).thenReturn(petAnalysis);
        when(petAnalysisRepository.save(petAnalysis)).thenReturn(petAnalysis);
        when(petAnalysisMapper.toResponse(petAnalysis)).thenReturn(responseDTO);
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        doNothing().when(sqsSenderService).sendMessage(analysisMessage);

        PetAnalysisResponseDTO result = petAnalysisService.createPetAnalysis(requestDTO,
            loggedUserKeycloakId);

        PetAnalysisService analysisSpy = spy(petAnalysisService);
        assertDoesNotThrow(() -> petAnalysisService.validatePetOwnershipOrAdmin(pet, user.getKeycloakId()));
        assertNotNull(result);
        assertEquals("http://example.com/picture.jpg", result.picture());
        assertEquals("Healthy", result.result());
        assertEquals(AnalysisType.BREED, result.analysisType());

        verify(petRepository).findById(1L);
        verify(petAnalysisRepository).save(petAnalysis);
    }

    @Test
    void createPetAnalysisWithInvalidPet() {
        PetAnalysisRequestDTO requestDTO = new PetAnalysisRequestDTO(-1L, mockImage, AnalysisType.BREED);

        when(petRepository.findById(any())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> petAnalysisService.createPetAnalysis(requestDTO, loggedUserKeycloakId));

        assertEquals("ID não pode ser negativo", exception.getMessage());

        verify(petAnalysisRepository, never()).save(any(PetAnalysis.class));
    }

    @Test
    void createPetAnalysisWithNullPet() {
        PetAnalysisRequestDTO requestDTO = new PetAnalysisRequestDTO(-1L, mockImage, AnalysisType.BREED);

        assertThrows(IllegalArgumentException.class,
            () -> petAnalysisService.createPetAnalysis(requestDTO, loggedUserKeycloakId),
            "ID não pode ser nulo");
    }

    @Test
    void shouldNotCreateWhenUserNotOwner(){
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

        PetAnalysisRequestDTO requestDTO = new PetAnalysisRequestDTO(1L, mockImage, AnalysisType.BREED);
        PetAnalysis petAnalysis = new PetAnalysis();
        PetAnalysisResponseDTO responseDTO = new PetAnalysisResponseDTO(1L, LocalDateTime.now(),  LocalDateTime.now(),"http://example.com/picture.jpg", "Healthy", 83.24,AnalysisType.BREED, AnalysisStatus.COMPLETED);

        AnalysisMessage analysisMessage = new AnalysisMessage(1L, "http", "DOG_BREED");

        when(petAnalysisMapper.toEntity(requestDTO)).thenReturn(petAnalysis);
        when(petAnalysisRepository.save(petAnalysis)).thenReturn(petAnalysis);
        when(petAnalysisMapper.toResponse(petAnalysis)).thenReturn(responseDTO);
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        doNothing().when(sqsSenderService).sendMessage(analysisMessage);

        PetAnalysisResponseDTO result = petAnalysisService.createPetAnalysis(requestDTO,
                loggedUserKeycloakId);

        assertThrows(
                AccessDeniedException.class,
                () -> petAnalysisService.createPetAnalysis(requestDTO, "NotOwner")
        );
    }

    @Test
    void shouldNotCreateWithInvalidAnalysisType() {
        ObjectMapper objectMapper = new ObjectMapper();
        String invalidJson = """
            {
                "petId": 1,
                "analysisType": "UKNOWN",
                "picture": "www.example.com/picture.jpg"
            }
        """;

        InvalidFormatException exception = assertThrows(
                InvalidFormatException.class,
                () -> objectMapper.readValue(invalidJson, PetAnalysisRequestDTO.class)
        );
        String errorMessage = exception.getMessage();
        assertTrue(
                errorMessage.contains("not one of the values accepted for Enum class: [BREED, EMOTIONAL]")
        );
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
        petAnalysis.setAnalysisType(AnalysisType.BREED);

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

    @Test
    void shouldNotDeleteWhenNotOwner(){
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
        petAnalysis.setAnalysisType(AnalysisType.BREED);

        when(petAnalysisRepository.findById(1L)).thenReturn(Optional.of(petAnalysis));
        when(petAnalysisRepository.existsById(1L)).thenReturn(true);

        assertThrows(
                AccessDeniedException.class,
                () -> petAnalysisService.deletePetAnalysis(1L, "NotOwner")
        );
    }

    // c

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
        analyse.setAnalysisType(AnalysisType.BREED);
        analyse.setResult("Healthy");
        analyse.setPicture("http://example.com/picture.jpg");

        analyses.add(analyse);
        Pageable pageable = PageRequest.of(0, 10);
        Page<PetAnalysis> petAnalysisPage = new PageImpl<>(analyses, pageable, analyses.size());

        when(petAnalysisRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(petAnalysisPage);
        when(petAnalysisMapper.toResponseList(analyses)).thenReturn(new ArrayList<>());
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        Page<PetAnalysisResponseDTO> response = petAnalysisService.getAllByPetId(1L,
            loggedUserKeycloakId, null, null, null, null, null, pageable);

        assertNotNull(response);
        verify(petAnalysisRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getPetAnalysesByPetIdWithInvalidId() {
        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(IllegalArgumentException.class,
            () -> petAnalysisService.getAllByPetId(-1L, loggedUserKeycloakId, LocalDate.of(2000, 5, 10),
                    LocalDate.of(2000, 5, 10), AnalysisType.BREED, AnalysisStatus.IN_ANALYSIS, "Happy", pageable),
            "ID não pode ser negativo");
    }

    @Test
    void getPetAnalysesByPetIdWithNullId() {
        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(IllegalArgumentException.class,
            () -> petAnalysisService.getAllByPetId(null, loggedUserKeycloakId, LocalDate.of(2000, 5, 10),
                    LocalDate.of(2000, 5, 10), AnalysisType.BREED, AnalysisStatus.IN_ANALYSIS, "Happy", pageable),
            "ID não pode ser nulo");
    }

    @Test
    void shouldNotGetAnalysisByPetWhenNotOwner() {
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
        Pageable pageable = PageRequest.of(0, 10);
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        assertThrows(
                AccessDeniedException.class,
                () -> petAnalysisService.getAllByPetId(1L, "NotOwner", LocalDate.of(2000,
                                5, 10), LocalDate.of(2000, 5, 10), AnalysisType.BREED, AnalysisStatus.IN_ANALYSIS, "Happy", pageable)
        );

    }

    // d

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
        analyses.setAnalysisType(AnalysisType.BREED);
        analyses.setResult("Healthy");
        analyses.setPicture("http://example.com/picture.jpg");

        PetAnalysisResponseDTO responseDTO = new PetAnalysisResponseDTO(
                1L, LocalDateTime.now(), LocalDateTime.now(), "http://example.com/picture.jpg",
                "Healthy", 90.0,AnalysisType.BREED, AnalysisStatus.COMPLETED);

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
        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(IllegalArgumentException.class,
            () -> petAnalysisService.getAllByPetId(null, loggedUserKeycloakId, LocalDate.of(2000,
                    5, 10), LocalDate.of(2000, 5, 10), AnalysisType.BREED, AnalysisStatus.IN_ANALYSIS, "Happy", pageable),
            "ID não pode ser nulo");
    }

    @Test
    void shouldNotGetPetAnalysisWhenNotOwner() {
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
        analyses.setAnalysisType(AnalysisType.BREED);
        analyses.setResult("Healthy");
        analyses.setPicture("http://example.com/picture.jpg");

        PetAnalysisResponseDTO responseDTO = new PetAnalysisResponseDTO(
                1L, LocalDateTime.now(), LocalDateTime.now(), "http://example.com/picture.jpg",
                "Healthy", 90.0,AnalysisType.BREED, AnalysisStatus.COMPLETED);

        when(petAnalysisRepository.findById(1L)).thenReturn(Optional.of(analyses));
        when(petAnalysisMapper.toResponse(analyses)).thenReturn(responseDTO);

        assertThrows(
                AccessDeniedException.class,
                () -> petAnalysisService.getPetAnalysis(1L, "NotOwner")
        );
    }
}