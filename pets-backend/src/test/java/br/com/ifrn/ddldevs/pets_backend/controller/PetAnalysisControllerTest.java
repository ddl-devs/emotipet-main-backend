package br.com.ifrn.ddldevs.pets_backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ifrn.ddldevs.pets_backend.SecurityTestConfig;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisType;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Gender;
import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Species;
import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import br.com.ifrn.ddldevs.pets_backend.domain.PetAnalysis;
import br.com.ifrn.ddldevs.pets_backend.domain.User;
import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.PetAnalysis.PetAnalysisRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.User.UserRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.mapper.PetAnalysisMapper;
import br.com.ifrn.ddldevs.pets_backend.mapper.PetMapper;
import br.com.ifrn.ddldevs.pets_backend.mapper.UserMapper;
import br.com.ifrn.ddldevs.pets_backend.repository.PetAnalysisRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.PetRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.UserRepository;
import br.com.ifrn.ddldevs.pets_backend.service.PetAnalysisService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "AWS_BUCKET_NAME=emotipet-bucket"
})
@ActiveProfiles("test")
@Import({SecurityTestConfig.class})
public class PetAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private PetAnalysisService petAnalysisService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private PetAnalysisMapper petAnalysisMapper;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private PetAnalysisRepository petAnalysisRepository;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private TokenUtils tokenUtils;

    @MockitoBean
    MockMultipartFile mockImage = new MockMultipartFile(
        "file",
        "user-picture.jpg",
        "image/jpeg",
        "fake-image-content".getBytes()
    );

    private User user;

    private Pet pet;

    private PetAnalysis petAnalysis;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        UserRequestDTO userRequest = new UserRequestDTO(
            "bell",
            "belll@email.com",
            "Bell",
            "Doe",
            LocalDate.of(1990, 1, 15),
            mockImage,
            "user!123"
        );

        user = userMapper.toEntity(userRequest);
        user.setKeycloakId(UUID.randomUUID().toString());
        user = userRepository.save(user);

        PetRequestDTO petRequest = new PetRequestDTO(
            "Apolo",
            Gender.MALE,
            LocalDate.of(2020, 1, 15),
            BigDecimal.valueOf(10.0),
            Species.DOG,
            "Labrador",
            30,
            mockImage
        );

        pet = petMapper.toEntity(petRequest);
        pet.setUser(user);
        pet = petRepository.save(pet);

        System.out.println("petId: " + pet.getId());

        PetAnalysisRequestDTO petAnalysisRequest = new PetAnalysisRequestDTO(
            pet.getId(),
            mockImage,
            AnalysisType.BREED
        );

        petAnalysis = petAnalysisMapper.toEntity(petAnalysisRequest);
        petAnalysis = petAnalysisRepository.save(petAnalysis);
    }

    @AfterEach
    @Transactional
    public void tearDown() {
        petAnalysisRepository.deleteAll();
        petRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve listar análises com sucesso")
    void shouldListPetAnalysisSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("admin"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/pet-analysis/")
                    .header("Authorization", "Bearer " + tokenString)
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Deve retornar Forbidden ao listar análises com token de client")
    void shouldListPetAnalysisNotSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("client"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/pet-analysis/")
                    .header("Authorization", "Bearer " + tokenString)
            )
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("Deve criar uma análise com sucesso")
    void shouldCreateAnalysisSuccessfully() throws Exception {
        petAnalysisRepository.deleteAll();

        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("client"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/pet-analysis/")
                    .file(mockImage)
                    .header("Authorization", "Bearer " + tokenString)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("analysisType", AnalysisType.BREED.toString())
                    .param("petId", "1")
            )
            .andExpect(MockMvcResultMatchers.status()
                .isOk());
    }

    @Test
    @DisplayName("Deve buscar uma análise com sucesso")
    void shouldGetAnalysisSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("client"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        mockMvc.perform(get("/pet-analysis/{id}", petAnalysis.getId())
                .header("Authorization", "Bearer " + tokenString))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve deletar uma análise com sucesso")
    void shouldDeleteAnalysisSuccessfully() throws Exception {
        String tokenString = tokenUtils.getToken(user.getEmail());
        Jwt jwt = tokenUtils.getJwt(tokenString, user, List.of("client"));
        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        Long recommendationId = 1L;
        mockMvc.perform(delete("/pet-analysis/{id}" + petAnalysis.getId())
                .header("Authorization", "Bearer " + tokenString))
            .andExpect(status().isNoContent());
    }
}