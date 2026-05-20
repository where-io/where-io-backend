package analu.whereio.adapters.in.web.converter;

import analu.whereio.adapters.in.web.dto.request.VisitaDtoRequest;
import analu.whereio.adapters.in.web.dto.response.VisitaDtoResponse;
import analu.whereio.application.model.Visita;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VisitaConverter {

    Visita toDomain(VisitaDtoRequest visitaDtoRequest);

    VisitaDtoResponse toResponse(Visita visita);
}
