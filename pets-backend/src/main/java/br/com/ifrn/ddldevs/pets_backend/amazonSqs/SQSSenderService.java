package br.com.ifrn.ddldevs.pets_backend.amazonSqs;

import br.com.ifrn.ddldevs.pets_backend.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.UUID;

@Service
public class SQSSenderService {
    private final SqsTemplate sqsTemplate;

    private final ObjectMapper objectMapper;

    private final String queueUrl;

    public SQSSenderService(SqsTemplate sqsTemplate) {
        this.sqsTemplate = sqsTemplate;
        this.objectMapper = new ObjectMapper();
        this.queueUrl = "https://sqs.us-east-2.amazonaws.com/209479262001/pets-analysis-fifo.fifo";
    }

    public void sendMessage(AnalysisMessage message) {
        try{
            String body = objectMapper.writeValueAsString(message);
            String uuid = UUID.randomUUID().toString();

            sqsTemplate.send(this.queueUrl, body);
        }catch (JsonProcessingException e) {
            throw new BusinessException("Erro ao enviar mensagem: " + e.getMessage());
        }
    }
}
