package analu.whereio.adapters.out.persistence.mapper;

import analu.whereio.adapters.out.persistence.entity.FriendshipEntity;
import analu.whereio.application.model.Friendship;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FriendshipPersistenceMapper {

    Friendship toDomain(FriendshipEntity entity);

    FriendshipEntity toEntity(Friendship friendship);
}
