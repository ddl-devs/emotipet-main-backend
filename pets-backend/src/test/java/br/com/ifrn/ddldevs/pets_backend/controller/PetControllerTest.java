package br.com.ifrn.ddldevs.pets_backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.Species;
import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import br.com.ifrn.ddldevs.pets_backend.domain.User;
import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.mapper.PetMapper;
import br.com.ifrn.ddldevs.pets_backend.repository.PetRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PetControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    @Transactional
    void setup() {
        petRepository.deleteAll();
        userRepository.deleteAll();

        User newUser = new User();
        newUser.setUsername("jhon");
        newUser.setFirstName("Jhon");
        newUser.setLastName("Doe");
        newUser.setEmail("jhon@gmail.com");
        newUser.setKeycloakId("345");
        newUser.setPets(new ArrayList<>());

        this.user = userRepository.save(newUser);
        userRepository.flush();
    }

    @Test
    @DisplayName("Should create a pet successfully")
    @Transactional
    void shouldCreatePetSuccessfully() throws Exception {

        PetRequestDTO dto = new PetRequestDTO();
        dto.setName("Apolo");
        dto.setSpecies(Species.DOG);
        dto.setHeight(30);
        dto.setWeight(BigDecimal.valueOf(10.0));

        String requestBody = objectMapper.writeValueAsString(dto);

        mockMvc.perform(
                post("/pets/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            ).andExpect(MockMvcResultMatchers.status().isCreated())
            .andDo(MockMvcResultHandlers.print());

        Optional<Pet> savedPet = petRepository.findAll().stream().findFirst();
        assertTrue(savedPet.isPresent());
        assertEquals("Apolo", savedPet.get().getName());
    }

    @Test
    @DisplayName("Should update a pet successfully")
    @Transactional
    void shouldUpdatePetSuccessfully() throws Exception {
        Pet pet = new Pet();
        pet.setName("Apolo");
        pet.setSpecies(Species.DOG);
        pet.setHeight(30);
        pet.setWeight(BigDecimal.valueOf(10.0));
        pet.setUser(this.user);
        pet = petRepository.save(pet);

        PetRequestDTO dto = new PetRequestDTO();
        dto.setName("Beagles");
        dto.setSpecies(Species.CAT);
        dto.setHeight(10);
        dto.setWeight(BigDecimal.valueOf(5.0));

        String requestBody = objectMapper.writeValueAsString(dto);

        mockMvc.perform(
            put("/pets/" + pet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        Optional<Pet> savedPet = petRepository.findById(pet.getId());
        assertTrue(savedPet.isPresent());
        assertEquals("Beagles", savedPet.get().getName());
    }

    @Test
    @DisplayName("Should delete a pet successfully")
    @Transactional
    void shouldDeletePetSuccessfully() throws Exception {
        Pet pet = new Pet();
        pet.setName("Apolo");
        pet.setSpecies(Species.DOG);
        pet.setHeight(30);
        pet.setWeight(BigDecimal.valueOf(10.0));
        pet.setUser(this.user);
        pet = petRepository.save(pet);

        petRepository.save(pet);

        mockMvc.perform(
            delete("/pets/" + pet.getId())
        ).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @Transactional
    @DisplayName("Should get a pet successfully")
    void shouldGetPetSuccessfully() throws Exception {
        Pet pet = new Pet();
        pet.setName("Apolo");
        pet.setSpecies(Species.DOG);
        pet.setHeight(30);
        pet.setWeight(BigDecimal.valueOf(10.0));
        pet.setUser(this.user);
        pet = petRepository.save(pet);

        petRepository.save(pet);

        String expectedResponse = objectMapper.writeValueAsString(petMapper.toPetResponseDTO(pet));

        mockMvc.perform(
                get("/pets/" + pet.getId())
            ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
    }

    @Test
    @Transactional
    @DisplayName("Should get a list of Pets successfully")
    void shouldGetPetsListSuccessfully() throws Exception {
        Pet pet1 = new Pet();
        pet1.setName("Apolo");
        pet1.setSpecies(Species.DOG);
        pet1.setHeight(30);
        pet1.setWeight(BigDecimal.valueOf(10.0));
        pet1.setUser(this.user);
        pet1 = petRepository.save(pet1);

        Pet pet2 = new Pet();
        pet2.setName("Beagles");
        pet2.setSpecies(Species.CAT);
        pet2.setHeight(10);
        pet2.setWeight(BigDecimal.valueOf(5.0));
        pet2.setUser(this.user);
        pet2 = petRepository.save(pet2);

        List<Pet> pets = new ArrayList<>();
        pets.add(pet1);
        pets.add(pet2);

        String expected_response = objectMapper.writeValueAsString(
            petMapper.toDTOList(pets)
        );

        mockMvc.perform(
                get("/pets/")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().json(expected_response));

    }
}