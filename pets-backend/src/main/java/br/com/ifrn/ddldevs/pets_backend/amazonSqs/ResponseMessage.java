package br.com.ifrn.ddldevs.pets_backend.amazonSqs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseMessage {
    private Long petID;
    private String result;
    private String status;
}
