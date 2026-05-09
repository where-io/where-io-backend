package analu.whereio.adapters.out.persistence.mapper;

import analu.whereio.adapters.out.persistence.entity.LocalEntity;
import analu.whereio.application.model.Local;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocalPersistenceMapper {

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "visitas", ignore = true)
    Local toDomain(LocalEntity localEntity);

    LocalEntity toEntity(Local local);

}