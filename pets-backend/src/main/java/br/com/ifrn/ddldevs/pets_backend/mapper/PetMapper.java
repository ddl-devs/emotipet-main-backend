package br.com.ifrn.ddldevs.pets_backend.mapper;

import br.com.ifrn.ddldevs.pets_backend.domain.Pet;
import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetRequestDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetResponseDTO;
import br.com.ifrn.ddldevs.pets_backend.dto.Pet.PetUpdateRequestDTO;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper
public interface PetMapper {

    PetResponseDTO toPetResponseDTO(Pet pet);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "photoUrl", ignore = true)
    Pet toEntity(PetRequestDTO petRequestDTO);

    List<PetResponseDTO> toDTOList(List<Pet> pets);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "photoUrl", ignore = true)
    void updateEntityFromDTO(PetUpdateRequestDTO petRequestDTO, @MappingTarget Pet pet);
}
