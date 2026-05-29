package analu.whereio.adapters.in.web.converter;

import analu.whereio.adapters.in.web.dto.request.local.LocalDtoRequest;
import analu.whereio.adapters.in.web.dto.request.local.LocalUpdateDtoRequest;
import analu.whereio.adapters.in.web.dto.response.LocalBuscarDtoResponse;
import analu.whereio.adapters.in.web.dto.response.LocalDtoResponse;
import analu.whereio.adapters.in.web.dto.response.PlaceDetailsDtoResponse;
import analu.whereio.adapters.in.web.dto.response.PredictionDto;
import analu.whereio.adapters.in.web.dto.response.StructuredFormattingDto;
import analu.whereio.adapters.out.external.geocoding.record.AutoCompleteResponse;
import analu.whereio.adapters.out.external.geocoding.record.PlaceDetailsRecord;
import analu.whereio.application.model.Local;
import analu.whereio.application.ports.out.FileStoragePort;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", uses = VisitaConverter.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LocalConverter {

    @Autowired
    protected FileStoragePort fileStoragePort;

    public abstract Local toDomain(LocalDtoRequest localDtoRequest);

    public abstract Local toDomain(LocalUpdateDtoRequest localUpdateDtoRequest);

    @Mapping(target = "fotoUrls", source = "fotos", qualifiedByName = "localFotosToUrls")
    public abstract LocalDtoResponse toResponse(Local local);

    @Named("localFotosToUrls")
    List<String> localFotosToUrls(List<String> fotos) {
        if (fotos == null || fotos.isEmpty()) {
            return List.of();
        }
        return fotos.stream().map(fileStoragePort::gerarUrlAssinada).toList();
    }

    public LocalBuscarDtoResponse toBuscarResponse(AutoCompleteResponse autoCompleteResponse) {
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

    public PlaceDetailsDtoResponse toPlaceDetailsResponse(PlaceDetailsRecord r) {
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
