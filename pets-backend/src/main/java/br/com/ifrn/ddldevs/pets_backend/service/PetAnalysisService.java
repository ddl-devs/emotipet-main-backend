package br.com.ifrn.ddldevs.pets_backend.service;


import br.com.ifrn.ddldevs.pets_backend.amazonSqs.AnalysisMessage;
import br.com.ifrn.ddldevs.pets_backend.amazonSqs.SQSSenderService;
import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import br.com.ifrn.ddldevs.pets_backend.domain.PetAnalysis;
import br.com.ifrn.ddldevs.pets_backend.dto.PetAnalysis.PetAnalysisRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.PetAnalysis.PetAnalysisResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.exception.ResourceNotFoundException;
import br.com.ifrn.ddldevs.pets_backend.mapper.PetAnalysisMapper;
import br.com.ifrn.ddldevs.pets_backend.repository.PetAnalysisRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.PetRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetAnalysisService {

    @Autowired
    private SQSSenderService senderService;

    @Autowired
    private PetAnalysisRepository petAnalysisRepository;

    @Autowired
    private PetAnalysisMapper petAnalysisMapper;

    @Autowired
    private PetRepository petRepository;

    @Transactional
    public PetAnalysisResponseDTO createPetAnalysis(PetAnalysisRequestDTO petAnalysisRequestDTO) {
        Long idPet = petAnalysisRequestDTO.getPetId();
        if (idPet == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (idPet < 0) {
            throw new IllegalArgumentException("ID não pode ser negativo");
        }

        PetAnalysis petAnalysis = petAnalysisMapper.toEntity(petAnalysisRequestDTO);
//        Pet pet = petRepository.findById(petAnalysisRequestDTO.getPetId())
//                .orElseThrow(() -> new RuntimeException("Pet not found"));
//
//        petAnalysis.setPet(pet);
//        pet.getPetAnalysis().add(petAnalysis);
//
//        petAnalysisRepository.save(petAnalysis);
//        petRepository.save(pet);

        AnalysisMessage message = new AnalysisMessage(
                petAnalysisRequestDTO.getPetId(),
                petAnalysisRequestDTO.getPicture(),
                petAnalysisRequestDTO.getAnalysisType()
        );
        senderService.sendMessage(message);

        return petAnalysisMapper.toResponse(petAnalysis);
    }

    public List<PetAnalysisResponseDTO> listPetAnalyses() {
        List<PetAnalysis> petAnalyses = petAnalysisRepository.findAll();
        return petAnalysisMapper.toResponseList(petAnalyses);
    }

    public void deletePetAnalysis(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (id < 0) {
            throw new IllegalArgumentException("ID não pode ser negativo");
        }
        if (!petAnalysisRepository.existsById(id)) {
            throw new ResourceNotFoundException("Análise não encontrada!");
        }
        petAnalysisRepository.deleteById(id);
    }

    public List<PetAnalysisResponseDTO> getAllByPetId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (id < 0) {
            throw new IllegalArgumentException("ID não pode ser negativo");
        }

        List<PetAnalysis> petAnalyses = petAnalysisRepository.findAllByPetId(id);
        return petAnalysisMapper.toResponseList(petAnalyses);
    }
    
    public PetAnalysisResponseDTO getPetAnalysis(Long analysisId) {
        if (analysisId == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (analysisId < 0) {
            throw new IllegalArgumentException("ID não pode ser negativo");
        }
        PetAnalysis petAnalysis = petAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new EntityNotFoundException("Pet Analysis not found"));
        return petAnalysisMapper.toResponse(petAnalysis);
    }
}