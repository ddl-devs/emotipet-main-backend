package br.com.ifrn.ddldevs.pets_backend.amazonSqs;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnalysisMessage {
    private Long analysisId;
    private String imageUrl;
    private String analysisType;
}
