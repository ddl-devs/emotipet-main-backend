package br.com.ifrn.ddldevs.pets_backend.amazonSqs;

import br.com.ifrn.ddldevs.pets_backend.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SQSSenderService {
    private final SqsTemplate sqsTemplate;

    private final ObjectMapper objectMapper;

    private final String queueUrl;

    public SQSSenderService(
            SqsTemplate sqsTemplate,
            @Value("${AWS_SQS_ANALYSIS_QUEUE_URL:https://sqs.us-east-2.amazonaws.com/209479262001/pets-analysis-fifo.fifo}") String queueUrl
    ) {
        this.sqsTemplate = sqsTemplate;
        this.objectMapper = new ObjectMapper();
        this.queueUrl = queueUrl;
    }

    public void sendMessage(AnalysisMessage message) {
        try{
            String body = objectMapper.writeValueAsString(message);

            sqsTemplate.send(this.queueUrl, body);
        }catch (JsonProcessingException e) {
            throw new BusinessException("Erro ao enviar mensagem: " + e.getMessage());
        }
    }
}
