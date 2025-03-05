package br.com.ifrn.ddldevs.pets_backend;

import static org.mockito.Mockito.mock;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
public class SecurityTestConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        return mock(JwtDecoder.class);
    }

    @Bean
    public SqsTemplate sqsTemplate() {
        return mock(SqsTemplate.class);
    }

}
