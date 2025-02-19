package br.com.ifrn.ddldevs.pets_backend.amazonSqs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnalysisMessage {
    private Long petId;
    private String imageUrl;
    private String analysisType;
}
