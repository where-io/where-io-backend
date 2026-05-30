package analu.whereio.adapters.in.web.converter;

import analu.whereio.adapters.in.web.dto.response.CompartilhamentoResponse;
import analu.whereio.application.model.SharingSettings;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompartilhamentoConverter {

    CompartilhamentoResponse toResponse(SharingSettings settings);
}
