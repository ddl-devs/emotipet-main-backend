package br.com.ifrn.ddldevs.pets_backend.mapper;

import br.com.ifrn.ddldevs.pets_backend.domain.Recommendation;
import br.com.ifrn.ddldevs.pets_backend.dto.Recommendation.RecommendationRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.Recommendation.RecommendationResponseDTO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper()
public interface RecommendationMapper {

    RecommendationResponseDTO toRecommendationResponseDTO(Recommendation recommendation);

    @Mapping(target = "pet", ignore = true)
    @Mapping(target = "id", ignore = true)
    Recommendation toEntity(RecommendationRequestDTO recommendationRequestDTO);

    List<RecommendationResponseDTO> toDTOList(List<Recommendation> recommendations);
}