package analu.whereio.adapters.in.web.converter;
import analu.whereio.adapters.in.web.dto.request.TagDtoRequest;
import analu.whereio.adapters.in.web.dto.response.TagDtoResponse;
import analu.whereio.application.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagConverter {
    Tag toDomain(TagDtoRequest request);
    TagDtoResponse toResponse(Tag tag);
}
