package br.com.ifrn.ddldevs.pets_backend.microservice;

import br.com.ifrn.ddldevs.pets_backend.domain.PetAnalysis;
import br.com.ifrn.ddldevs.pets_backend.domain.Recommendation;
import br.com.ifrn.ddldevs.pets_backend.repository.PetAnalysisRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class RecommendationRequestsService {
    @Autowired
    private PetAnalysisRepository petAnalysisRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String recommendationUrl;

    public RecommendationRequestsService(
            @Value("${MICROSERVICE_BASE_URL:http://127.0.0.1:8000}") String baseUrl
    ) {
        this.recommendationUrl = baseUrl + "/recommendations/";
    }

    private void sendRequest(Recommendation recommendation, String suffix, Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            String jsonResponse = restTemplate.postForObject(recommendationUrl + suffix, request, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            String recommendationText = rootNode.path("response").asText();

            recommendation.setRecommendation(recommendationText);
        } catch (Exception e) {
            recommendation.setRecommendation("Erro ao obter recomendação: " + e.getMessage());
        }
    }

    private Map<String, Object> prepareCommonRequest(Recommendation recommendation) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("species", recommendation.getPet().getSpecies());
        requestBody.put("breed", recommendation.getPet().getBreed());
        requestBody.put("weight", recommendation.getPet().getWeight());
        requestBody.put("age", recommendation.getPet().getAge());
        return requestBody;
    }

    private Map<String, Object> prepareWithEmotionRequest(Recommendation recommendation) {
        Map<String, Object> requestBody = prepareCommonRequest(recommendation);

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1L);
        List<PetAnalysis> analyses = petAnalysisRepository.findRecentEmotionalAnalyses(
                recommendation.getPet().getId(), oneMonthAgo
        );

        if (analyses != null && !analyses.isEmpty()) {
            List<Map<String, Object>> emotions = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            for (PetAnalysis analysis : analyses) {
                Map<String, Object> emotionData = new HashMap<>();
                emotionData.put("emotion", analysis.getResult());
                emotionData.put("accuracy", analysis.getAccuracy());

                String formattedDate = analysis.getCreatedAt().format(formatter);
                emotionData.put("date", formattedDate);

                emotions.add(emotionData);
            }
            requestBody.put("emotions", emotions);
        }

        return requestBody;
    }

    private Map<String, Object> prepareIMCRequest(Recommendation recommendation) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("species", recommendation.getPet().getSpecies());
        requestBody.put("breed", recommendation.getPet().getBreed());
        requestBody.put("weight", recommendation.getPet().getWeight());
        requestBody.put("height", recommendation.getPet().getHeight());
        return requestBody;
    }

    public void updateRecommendation(Recommendation recommendation) {
        switch (recommendation.getCategoryRecommendation()) {
            case IMC:
                sendRequest(recommendation, "imc", prepareIMCRequest(recommendation));
                break;
            case HEALTH:
            case ACTIVITIES:
                sendRequest(recommendation, recommendation.getCategoryRecommendation().name().toLowerCase(), prepareWithEmotionRequest(recommendation));
                break;
            case TRAINING:
            case PRODUCTS:
                sendRequest(recommendation, recommendation.getCategoryRecommendation().name().toLowerCase(), prepareCommonRequest(recommendation));
                break;
            default:
                recommendation.setRecommendation("Categoria não suportada");
                break;
        }
    }
}
