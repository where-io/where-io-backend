package analu.whereio.adapters.out.persistence.mapper;

import analu.whereio.adapters.out.persistence.entity.UserEntity;
import analu.whereio.application.model.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserAccountPersistenceMapper {

    @Mapping(source = "passwordHash", target = "encodedPassword")
    UserAccount toDomain(UserEntity entity);

    @Mapping(source = "encodedPassword", target = "passwordHash")
    UserEntity toEntity(UserAccount domain);
}
