package analu.whereio.adapters.out.persistence.mapper;
import analu.whereio.adapters.out.persistence.entity.TagEntity;
import analu.whereio.application.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagPersistenceMapper {
    Tag toDomain(TagEntity entity);
    TagEntity toEntity(Tag tag);
}
