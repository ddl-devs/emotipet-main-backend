package br.com.ifrn.ddldevs.pets_backend.amazonSqs;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisStatus;
import br.com.ifrn.ddldevs.pets_backend.domain.PetAnalysis;
import br.com.ifrn.ddldevs.pets_backend.exception.ResourceNotFoundException;
import br.com.ifrn.ddldevs.pets_backend.repository.PetAnalysisRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;

import org.springframework.stereotype.Component;

@Component
public class SQSListenerComponent {

    private final PetAnalysisRepository petAnalysisRepository;

    public SQSListenerComponent(PetAnalysisRepository petAnalysisRepository) {
        this.petAnalysisRepository = petAnalysisRepository;
    }

    @SqsListener("pets-responses-fifo.fifo")
    public void receiveMessage(AnalysisResponseMessage message) {
        PetAnalysis petAnalysis = petAnalysisRepository.findById(message.getAnalysisId())
                .orElseThrow(() -> new ResourceNotFoundException("Análise não econtrada"));
        if (message.getStatus().startsWith("2")){
            petAnalysis.setResult(message.getResult());
            petAnalysis.setAnalysisStatus(AnalysisStatus.COMPLETED);
        }else{
            petAnalysis.setResult(message.getResult());
            petAnalysis.setAnalysisStatus(AnalysisStatus.FAILURE);
        }
    }
}
