package br.com.ifrn.ddldevs.pets_backend.mapper;

import br.com.ifrn.ddldevs.pets_backend.domain.PetAnalysis;
import br.com.ifrn.ddldevs.pets_backend.dto.PetAnalysis.PetAnalysisRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.PetAnalysis.PetAnalysisResponseDTO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PetAnalysisMapper {

    PetAnalysisResponseDTO toResponse(PetAnalysis petAnalysis);

    @Mapping(target = "pet", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "picture", ignore = true)
    PetAnalysis toEntity(PetAnalysisRequestDTO petAnalysisRequestDTO);

    List<PetAnalysisResponseDTO> toResponseList(List<PetAnalysis> petAnalyses);

}