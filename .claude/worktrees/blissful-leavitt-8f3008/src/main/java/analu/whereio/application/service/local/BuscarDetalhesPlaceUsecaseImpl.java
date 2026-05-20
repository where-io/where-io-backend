package analu.whereio.application.service.local;

import analu.whereio.adapters.out.external.geocoding.record.PlaceDetailsRecord;
import analu.whereio.application.ports.in.local.BuscarDetalhesPlaceUsecase;
import analu.whereio.application.ports.out.PlacesApiPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class BuscarDetalhesPlaceUsecaseImpl implements BuscarDetalhesPlaceUsecase {

    private static final Logger log = LoggerFactory.getLogger(BuscarDetalhesPlaceUsecaseImpl.class);

    private final PlacesApiPort placesApiPort;

    @Override
    public PlaceDetailsRecord execute(String placeId) {
        MDC.put("operation", "buscarDetalhesPlace");
        try {
            if (!StringUtils.hasText(placeId)) {
                throw new BusinessException("placeId obrigatório", HttpStatus.BAD_REQUEST);
            }
            return placesApiPort.placeDetails(placeId.trim());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao obter detalhes do place. placeId={} erro={}", placeId, e.getMessage(), e);
            throw new BusinessException("Não foi possível obter o endereço selecionado", HttpStatus.UNPROCESSABLE_ENTITY);
        } finally {
            MDC.remove("operation");
        }
    }
}
