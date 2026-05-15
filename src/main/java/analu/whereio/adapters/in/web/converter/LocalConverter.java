package analu.whereio.adapters.in.web.converter;

import analu.whereio.adapters.in.web.dto.request.LocalDtoRequest;
import analu.whereio.adapters.in.web.dto.response.LocalBuscarDtoResponse;
import analu.whereio.adapters.in.web.dto.response.LocalDtoResponse;
import analu.whereio.adapters.out.external.geocoding.record.AutoCompleteResponse;
import analu.whereio.application.model.Local;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = VisitaConverter.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocalConverter {

    Local toDomain(LocalDtoRequest localDtoRequest);

    @Mapping(target = "fotoUrls", source = "fotos", qualifiedByName = "localFotosToUrls")
    LocalDtoResponse toResponse(Local local);

    @Named("localFotosToUrls")
    default List<String> localFotosToUrls(List<String> fotos) {
        if (fotos == null || fotos.isEmpty()) {
            return List.of();
        }
        return fotos.stream().map(f -> "/media/" + f).toList();
    }
    LocalBuscarDtoResponse toBuscarResponse(AutoCompleteResponse autoCompleteResponse);
}
