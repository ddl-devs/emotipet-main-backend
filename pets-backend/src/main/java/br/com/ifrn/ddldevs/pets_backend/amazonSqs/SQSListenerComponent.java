package br.com.ifrn.ddldevs.pets_backend.amazonSqs;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisStatus;
import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import br.com.ifrn.ddldevs.pets_backend.domain.PetAnalysis;
import br.com.ifrn.ddldevs.pets_backend.exception.ResourceNotFoundException;
import br.com.ifrn.ddldevs.pets_backend.repository.PetAnalysisRepository;
import br.com.ifrn.ddldevs.pets_backend.repository.PetRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SQSListenerComponent {

    private final PetAnalysisRepository petAnalysisRepository;

    private final PetRepository petRepository;

    public SQSListenerComponent(
            PetAnalysisRepository petAnalysisRepository,
            PetRepository petRepository
    ) {
        this.petAnalysisRepository = petAnalysisRepository;
        this.petRepository = petRepository;
    }

    @SqsListener("${AWS_SQS_ANALYSIS_RESPONSE_NAME:pets-responses-fifo.fifo}")
    public void receiveMessage(AnalysisResponseMessage message) {
        PetAnalysis petAnalysis = petAnalysisRepository.findById(message.getAnalysisId())
                .orElseThrow(() -> new ResourceNotFoundException("Análise não econtrada"));
        if (message.getStatus().startsWith("2")){
            petAnalysis.setResult(message.getResult());
            petAnalysis.setAnalysisStatus(AnalysisStatus.COMPLETED);
            petAnalysis.setAccuracy(message.getAccuracy());

            if(message.getAnalysisType().endsWith("BREED")){
                Pet pet = petRepository.findById(petAnalysis.getPet().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pet não econtrado"));
                    pet.setBreed(petAnalysis.getResult());
                petRepository.save(pet);
            }
        }else{
            petAnalysis.setResult(message.getResult());
            petAnalysis.setAnalysisStatus(AnalysisStatus.FAILURE);
        }
        petAnalysisRepository.save(petAnalysis);
    }
}
