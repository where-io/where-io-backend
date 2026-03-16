package analu.whereio.adapters.in.web.converter;

import analu.whereio.adapters.in.web.dto.request.LocalDtoRequest;
import analu.whereio.adapters.in.web.dto.response.LocalBuscarDtoResponse;
import analu.whereio.adapters.in.web.dto.response.LocalDtoResponse;
import analu.whereio.adapters.out.external.geocoding.record.AutoCompleteResponse;
import analu.whereio.application.model.Local;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = VisitaConverter.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocalConverter {

    Local toDomain(LocalDtoRequest localDtoRequest);
    LocalDtoResponse toResponse(Local local);
    LocalBuscarDtoResponse toBuscarResponse(AutoCompleteResponse autoCompleteResponse);
}
