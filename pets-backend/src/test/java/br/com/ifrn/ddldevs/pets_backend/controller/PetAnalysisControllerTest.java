package br.com.ifrn.ddldevs.pets_backend.controller;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Species;
import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import br.com.ifrn.ddldevs.pets_backend.domain.PetAnalysis;
import br.com.ifrn.ddldevs.pets_backend.domain.User;
import br.com.ifrn.ddldevs.pets_backend.dto.PetAnalysis.PetAnalysisRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.mapper.PetAnalysisMapper;
import br.com.ifrn.ddldevs.pets_backend.repository.PetAnalysisRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.PetRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PetAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PetAnalysisRepository petAnalysisRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private PetAnalysisMapper petAnalysisMapper;

    private Pet pet;

    private User user;

    @BeforeEach
    @Transactional
    public void setUp() {
        petAnalysisRepository.deleteAll();
        petRepository.deleteAll();

        User new_user = new User();
        new_user.setUsername("jhon");
        new_user.setFirstName("Jhon");
        new_user.setLastName("Doe");
        new_user.setEmail("jhon@gmail.com");
        new_user.setKeycloakId("345");
        new_user.setPets(new ArrayList<>());

        Pet pet = new Pet();
        pet.setName("Apolo");
        pet.setSpecies(Species.DOG);
        pet.setHeight(30);
        pet.setWeight(BigDecimal.valueOf(10.0));

        new_user.getPets().add(pet);
        pet.setUser(new_user);

        this.user = userRepository.save(new_user);
        this.pet = petRepository.save(pet);
    }

    @Test
    @DisplayName("Should create a pet analysis successfully")
    @Transactional
    public void createPetAnalysisSuccessfully() throws Exception {
        PetAnalysisRequestDTO requestDTO = new PetAnalysisRequestDTO(this.pet.getId(),
            "http://example.com/picture.jpg", AnalysisType.DOG_BREED);
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/pet-analysis/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(MockMvcResultMatchers.status().isCreated());

        List<PetAnalysis> analyses = petAnalysisRepository.findAll();
        assertEquals(1, analyses.size());
        assertEquals("Healthy", analyses.getFirst().getResult());
    }

    @Test
    @DisplayName("Should delete a pet analysis successfully")
    @Transactional
    public void deletePetAnalysisSuccessfully() throws Exception {
        PetAnalysis petAnalysis = new PetAnalysis();
        petAnalysis.setPet(this.pet);
        petAnalysis.setResult("Happy");
        petAnalysis.setPicture("www.pic.com");
        petAnalysis.setAnalysisType(AnalysisType.DOG_BREED);
        petAnalysis = petAnalysisRepository.save(petAnalysis);

        mockMvc.perform(delete("/pet-analysis/" + petAnalysis.getId()))
            .andExpect(MockMvcResultMatchers.status().isNoContent());

        assertFalse(petAnalysisRepository.existsById(petAnalysis.getId()));
    }

    @Test
    @DisplayName("Should get a pet analysis by id successfully")
    @Transactional
    public void getPetAnalysisByIdSuccessfully() throws Exception {
        PetAnalysis petAnalysis = new PetAnalysis();
        petAnalysis.setPet(this.pet);
        petAnalysis.setResult("Happy");
        petAnalysis.setPicture("www.pic.com");
        petAnalysis.setAnalysisType(AnalysisType.DOG_BREED);
        petAnalysis = petAnalysisRepository.save(petAnalysis);

        String expectedResponse = objectMapper.writeValueAsString(
            petAnalysisMapper.toResponse(petAnalysis));

        mockMvc.perform(get("/pet-analysis/" + petAnalysis.getId()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
    }

    @Test
    @DisplayName("Should list all pet analyses successfully")
    @Transactional
    public void listAllPetAnalysesSuccessfully() throws Exception {
        PetAnalysis petAnalysis = new PetAnalysis();
        petAnalysis.setPet(this.pet);
        petAnalysis.setResult("Happy");
        petAnalysis.setPicture("www.pic.com");
        petAnalysis.setAnalysisType(AnalysisType.DOG_BREED);
        petAnalysis = petAnalysisRepository.save(petAnalysis);

        PetAnalysis petAnalysis2 = new PetAnalysis();
        petAnalysis2.setPet(this.pet);
        petAnalysis2.setResult("Sad");
        petAnalysis2.setPicture("www.pic.com");
        petAnalysis2.setAnalysisType(AnalysisType.DOG_BREED);
        petAnalysis2 = petAnalysisRepository.save(petAnalysis2);

        List<PetAnalysis> petAnalyses = petAnalysisRepository.findAll();
        String expectedResponse = objectMapper.writeValueAsString(
            petAnalysisMapper.toResponseList(petAnalyses));

        mockMvc.perform(get("/pet-analysis/"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
    }


    @Test
    @DisplayName("Should list all pet analyses successfully")
    @Transactional
    public void getPetAnalysisByPetIdSuccessfully() throws Exception {

        Pet cat = new Pet();
        cat.setName("Beagles");
        cat.setSpecies(Species.CAT);
        cat.setHeight(30);
        cat.setWeight(BigDecimal.valueOf(10.0));
        cat.setUser(this.user);
        cat = petRepository.save(cat);

        PetAnalysis petAnalysis = new PetAnalysis();
        petAnalysis.setPet(cat);
        petAnalysis.setResult("Angry");
        petAnalysis.setPicture("www.pic.com");
        petAnalysis.setAnalysisType(AnalysisType.CAT_BREED);
        petAnalysis = petAnalysisRepository.save(petAnalysis);

        PetAnalysis petAnalysis2 = new PetAnalysis();
        petAnalysis2.setPet(this.pet);
        petAnalysis2.setResult("Sad");
        petAnalysis2.setPicture("www.pic.com");
        petAnalysis2.setAnalysisType(AnalysisType.CAT_BREED);
        petAnalysis2 = petAnalysisRepository.save(petAnalysis2);

        List<PetAnalysis> petAnalysisList = new ArrayList<>();
        petAnalysisList.add(petAnalysis);

        String expectedResponse = objectMapper.writeValueAsString(
            petAnalysisMapper.toResponseList(petAnalysisList));
        System.out.println(expectedResponse);
        mockMvc.perform(get("/pet-analysis/pet/" + cat.getId()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
    }
}