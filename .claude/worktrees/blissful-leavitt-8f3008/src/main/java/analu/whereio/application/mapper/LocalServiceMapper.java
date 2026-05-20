package analu.whereio.application.mapper;

import analu.whereio.adapters.out.persistence.entity.LocalEntity;
import analu.whereio.application.model.Local;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocalServiceMapper {

    LocalEntity toEntity(Local local);

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "visitas", ignore = true)
    Local toModel(LocalEntity localEntity);
}
