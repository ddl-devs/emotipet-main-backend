package br.com.ifrn.ddldevs.pets_backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.RecommendationCategories;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Species;
import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import br.com.ifrn.ddldevs.pets_backend.domain.Recommendation;
import br.com.ifrn.ddldevs.pets_backend.domain.User;
import br.com.ifrn.ddldevs.pets_backend.dto.PetAnalysis.PetAnalysisRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.Recommendation.RecommendationRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.Recommendation.RecommendationResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.exception.AccessDeniedException;
import br.com.ifrn.ddldevs.pets_backend.exception.ResourceNotFoundException;
import br.com.ifrn.ddldevs.pets_backend.mapper.RecommendationMapper;
import br.com.ifrn.ddldevs.pets_backend.microservice.RecommendationRequestsService;
import br.com.ifrn.ddldevs.pets_backend.repository.PetRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.RecommendationRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class RecommendationsServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private RecommendationRequestsService recommendationRequestsService;

    @Mock
    private RecommendationMapper recommendationMapper;

    @InjectMocks
    private RecommendationService recommendationService;

    private final String loggedUserKeycloakId = "1abc23";

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
    void createRecommendationWithValidPet() {
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

        RecommendationRequestDTO requestDTO = new RecommendationRequestDTO(
            1L,
            RecommendationCategories.HEALTH
        );
        Recommendation recommendation = new Recommendation();
        recommendation.setCategoryRecommendation(RecommendationCategories.HEALTH);
        recommendation.setRecommendation("Feed your pet twice daily");
        recommendation.setPet(pet);


        RecommendationResponseDTO responseDTO = new RecommendationResponseDTO(
            1L,
            LocalDateTime.now(),
            LocalDateTime.now(),
            "Feed your pet twice daily",
            RecommendationCategories.HEALTH
        );

        doNothing().when(recommendationRequestsService).updateRecommendation(recommendation);
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(recommendationMapper.toEntity(requestDTO)).thenReturn(recommendation);
        when(recommendationRepository.save(recommendation)).thenReturn(recommendation);
        when(recommendationMapper.toRecommendationResponseDTO(recommendation)).thenReturn(
            responseDTO);

        RecommendationResponseDTO result = recommendationService.createRecommendation(requestDTO,
            loggedUserKeycloakId);

        assertNotNull(result);
        assertEquals("Feed your pet twice daily", result.recommendation());
        assertEquals(RecommendationCategories.HEALTH, result.categoryRecommendation());

        verify(petRepository).findById(1L);
        verify(recommendationRepository).save(recommendation);
    }

    @Test
    void createRecommendationWithInvalidIdPet() {
        RecommendationRequestDTO requestDTO = new RecommendationRequestDTO(-1L, RecommendationCategories.HEALTH);

        when(petRepository.findById(-1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> recommendationService.createRecommendation(requestDTO, loggedUserKeycloakId));

        assertEquals("ID não pode ser negativo", exception.getMessage());

        verify(recommendationRepository, never()).save(any(Recommendation.class));
    }

    @Test
    void createRecommendationWithNullIDPet() {
        RecommendationRequestDTO requestDTO = new RecommendationRequestDTO(null, RecommendationCategories.HEALTH);

        when(petRepository.findById(null)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> recommendationService.createRecommendation(requestDTO, loggedUserKeycloakId));

        assertEquals("ID não pode ser nulo", exception.getMessage());

        verify(recommendationRepository, never()).save(any(Recommendation.class));
    }

    @Test
    void shouldNotCreateRecommendationWhenInvalidCategory() {
        ObjectMapper objectMapper = new ObjectMapper();
        String invalidJson = """
            {
                "petId": 1,
                "categoryRecommendation": "UKNOWN",
            }
        """;

        InvalidFormatException exception = assertThrows(
                InvalidFormatException.class,
                () -> objectMapper.readValue(invalidJson, RecommendationRequestDTO.class)
        );
        String errorMessage = exception.getMessage();
        assertTrue(
                errorMessage.contains("not one of the values accepted for Enum class: [TRAINING, PRODUCTS, ACTIVITIES, IMC, HEALTH]")
        );
    }

    @Test
    void shouldNotCreateRecommendationWhenNotOwner(){
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

        RecommendationRequestDTO requestDTO = new RecommendationRequestDTO(
                1L,
                RecommendationCategories.HEALTH
        );
        Recommendation recommendation = new Recommendation();
        recommendation.setCategoryRecommendation(RecommendationCategories.HEALTH);
        recommendation.setRecommendation("Feed your pet twice daily");
        recommendation.setPet(pet);


        RecommendationResponseDTO responseDTO = new RecommendationResponseDTO(
                1L,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Feed your pet twice daily",
                RecommendationCategories.HEALTH
        );

        doNothing().when(recommendationRequestsService).updateRecommendation(recommendation);
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(recommendationMapper.toEntity(requestDTO)).thenReturn(recommendation);
        when(recommendationRepository.save(recommendation)).thenReturn(recommendation);
        when(recommendationMapper.toRecommendationResponseDTO(recommendation)).thenReturn(
                responseDTO);

        assertThrows(
                AccessDeniedException.class,
                () -> recommendationService.createRecommendation(requestDTO, "NotOwner")
        );
    }

    @Test
    void shouldCreateWhenPetNotFound(){
        RecommendationRequestDTO requestDTO = new RecommendationRequestDTO(
                1L,
                RecommendationCategories.HEALTH
        );
        when(recommendationMapper.toEntity(any())).thenReturn(null);
        when(petRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> recommendationService.createRecommendation(requestDTO, loggedUserKeycloakId)
        );
    }

    // b

    @Test
    void deleteRecommendationWithValidId() {
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

        Recommendation recommendation = new Recommendation();
        recommendation.setId(1L);
        recommendation.setRecommendation("Feed your pet twice daily");
        recommendation.setCategoryRecommendation(RecommendationCategories.HEALTH);
        recommendation.setCreatedAt(LocalDateTime.now());
        recommendation.setPet(pet);

        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(recommendation));
        when(recommendationRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(
            () -> recommendationService.deleteRecommendation(1L, loggedUserKeycloakId));

        verify(recommendationRepository).deleteById(1L);
    }

    @Test
    void deleteRecommendationWithIdNull() {
        assertThrows(IllegalArgumentException.class,
            () -> recommendationService.deleteRecommendation(null, loggedUserKeycloakId),
            "ID não pode ser nulo");
    }

    @Test
    void deleteRecommendationWithInvalidId() {
        assertThrows(IllegalArgumentException.class,
            () -> recommendationService.deleteRecommendation(-1L, loggedUserKeycloakId),
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
        pet.setId(1L);
        pet.setName("Apolo");
        pet.setSpecies(Species.DOG);
        pet.setHeight(30);
        pet.setWeight(BigDecimal.valueOf(10.0));
        pet.setUser(user);

        Recommendation recommendation = new Recommendation();
        recommendation.setId(1L);
        recommendation.setRecommendation("Feed your pet twice daily");
        recommendation.setCategoryRecommendation(RecommendationCategories.HEALTH);
        recommendation.setCreatedAt(LocalDateTime.now());
        recommendation.setPet(pet);

        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(recommendation));

        assertThrows(
                AccessDeniedException.class,
                () -> recommendationService.deleteRecommendation(1L, "NotOwner")
        );
    }

    @Test
    void shouldNotDeleteWhenNotFound(){
        when(recommendationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> recommendationService.deleteRecommendation(1L, "NotOwner")
        );
    }

    // c

    @Test
    void getRecommendationsByPetIdWithValidId() {
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

        Recommendation recommendation = new Recommendation();
        recommendation.setId(1L);
        recommendation.setPet(pet);
        recommendation.setCategoryRecommendation(RecommendationCategories.HEALTH);
        recommendation.setRecommendation("Lorem Ipsum");

        List<Recommendation> recommendations = new ArrayList<>();
        recommendations.add(recommendation);

        when(recommendationRepository.findAllByPetId(1L)).thenReturn(recommendations);
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(recommendationMapper.toDTOList(recommendations)).thenReturn(new ArrayList<>());

        List<RecommendationResponseDTO> response = recommendationService.getAllByPetId(1L,
            loggedUserKeycloakId);

        assertNotNull(response);
        verify(recommendationRepository).findAllByPetId(1L);
    }

    @Test
    void getRecommendationByPetWithInvalidId() {
        assertThrows(IllegalArgumentException.class,
            () -> recommendationService.getAllByPetId(-1L, loggedUserKeycloakId),
            "ID não pode ser negativo");
    }

    @Test
    void getRecommendationByPetWithNullId() {
        assertThrows(IllegalArgumentException.class,
            () -> recommendationService.getAllByPetId(null, loggedUserKeycloakId),
            "ID não pode ser nulo");
    }

    @Test
    void shouldNotReturnWhenPetNotFound() {
        when(petRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> recommendationService.getAllByPetId(999L, loggedUserKeycloakId)
        );
    }

    @Test
    void shouldNotReturnWhenNotOwner() {
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

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        assertThrows(
                AccessDeniedException.class,
                () -> recommendationService.getAllByPetId(1L, "NotOwner")
        );
    };

    // d

    @Test
    void getRecommendationWithValidId() {
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

        Recommendation recommendation = new Recommendation();
        recommendation.setId(1L);
        recommendation.setRecommendation("Feed your pet twice daily");
        recommendation.setCategoryRecommendation(RecommendationCategories.HEALTH);
        recommendation.setCreatedAt(LocalDateTime.now());
        recommendation.setPet(pet);

        RecommendationResponseDTO responseDTO = new RecommendationResponseDTO(
            1L,
            LocalDateTime.now(),
            LocalDateTime.now(),
            "Feed your pet twice daily",
            RecommendationCategories.HEALTH
        );

        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(recommendation));
        when(recommendationMapper.toRecommendationResponseDTO(recommendation)).thenReturn(
            responseDTO);

        RecommendationResponseDTO result = recommendationService.getRecommendation(1L,
            loggedUserKeycloakId);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Feed your pet twice daily", result.recommendation());
    }

    @Test
    void getRecommendationWithInvalidId() {
        assertThrows(IllegalArgumentException.class,
            () -> recommendationService.getRecommendation(-1L, loggedUserKeycloakId),
            "ID não pode ser negativo");
    }

    @Test
    void getRecommentationWithNullId() {
        assertThrows(IllegalArgumentException.class,
            () -> recommendationService.getRecommendation(null, loggedUserKeycloakId),
            "ID não pode ser nulo");
    }

    @Test
    void shouldNotReturnWhenNotFound() {
        when(recommendationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> recommendationService.getRecommendation(999L, loggedUserKeycloakId)
        );
    }

    @Test
    void shouldNotReturnRecommendationWhenNotOwner(){
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

        Recommendation recommendation = new Recommendation();
        recommendation.setId(1L);
        recommendation.setRecommendation("Feed your pet twice daily");
        recommendation.setCategoryRecommendation(RecommendationCategories.HEALTH);
        recommendation.setCreatedAt(LocalDateTime.now());
        recommendation.setPet(pet);

        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(recommendation));

        assertThrows(
                AccessDeniedException.class,
                () -> recommendationService.getRecommendation(1L, "NotOwner")
        );

    }

}
