package br.com.ifrn.ddldevs.pets_backend.amazonSqs;

import io.awspring.cloud.sqs.annotation.SqsListener;

import org.springframework.stereotype.Component;

@Component
public class SQSListenerComponent {
    @SqsListener("pets-responses-fifo.fifo")
    public void receiveMessage(String message) {
        System.out.println("Mensagem recebida: " + message);
    }
}
