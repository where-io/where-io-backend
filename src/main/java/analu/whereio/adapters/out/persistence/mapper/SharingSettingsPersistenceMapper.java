package analu.whereio.adapters.out.persistence.mapper;

import analu.whereio.adapters.out.persistence.entity.SharingSettingsEntity;
import analu.whereio.application.model.SharingSettings;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SharingSettingsPersistenceMapper {

    SharingSettings toDomain(SharingSettingsEntity entity);

    SharingSettingsEntity toEntity(SharingSettings settings);
}
