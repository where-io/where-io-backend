package analu.whereio.adapters.in.web.converter;

import analu.whereio.adapters.in.web.dto.request.LocalDtoRequest;
import analu.whereio.adapters.in.web.dto.response.LocalBuscarDtoResponse;
import analu.whereio.adapters.in.web.dto.response.LocalDtoResponse;
import analu.whereio.adapters.in.web.dto.response.PlaceDetailsDtoResponse;
import analu.whereio.adapters.in.web.dto.response.PredictionDto;
import analu.whereio.adapters.out.external.geocoding.record.PlaceDetailsRecord;
import analu.whereio.adapters.in.web.dto.response.StructuredFormattingDto;
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

    default LocalBuscarDtoResponse toBuscarResponse(AutoCompleteResponse autoCompleteResponse) {
        LocalBuscarDtoResponse out = new LocalBuscarDtoResponse();
        if (autoCompleteResponse == null || autoCompleteResponse.suggestions() == null) {
            out.setPredictions(List.of());
            return out;
        }
        out.setPredictions(autoCompleteResponse.suggestions().stream().map(s -> {
            PredictionDto p = new PredictionDto();
            p.setPlaceId(s.placeId());
            p.setDescription(s.description());
            StructuredFormattingDto sf = new StructuredFormattingDto();
            sf.setMainText(s.mainText());
            sf.setSecondaryText(s.secondaryText());
            p.setStructuredFormatting(sf);
            return p;
        }).toList());
        return out;
    }

    default PlaceDetailsDtoResponse toPlaceDetailsResponse(PlaceDetailsRecord r) {
        PlaceDetailsDtoResponse o = new PlaceDetailsDtoResponse();
        o.setLat(r.lat());
        o.setLng(r.lng());
        o.setLogradouro(r.logradouro());
        o.setBairro(r.bairro());
        o.setCidade(r.cidade());
        o.setEstado(r.estado());
        o.setCep(r.cep());
        o.setPais(r.pais());
        o.setFormattedAddress(r.formattedAddress());
        return o;
    }
}
