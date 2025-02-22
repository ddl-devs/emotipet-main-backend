package br.com.ifrn.ddldevs.pets_backend.amazonSqs;

import br.com.ifrn.ddldevs.pets_backend.domain.Enums.AnalysisType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AnalysisResponseMessage {
    private Long analysisId;
    private String result;
    private String status;
    private String analysisType;
    private Double accuracy;
}
