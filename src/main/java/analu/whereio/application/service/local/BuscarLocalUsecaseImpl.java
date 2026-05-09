package analu.whereio.application.service.local;

import analu.whereio.adapters.out.external.geocoding.record.AutoCompleteResponse;
import analu.whereio.application.ports.in.local.BuscarLocalUsecase;
import analu.whereio.application.ports.out.LatitudeLongitudeInterfacePort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor

public class BuscarLocalUsecaseImpl implements BuscarLocalUsecase {

    private static final Logger log = LoggerFactory.getLogger(BuscarLocalUsecaseImpl.class);

    private final LatitudeLongitudeInterfacePort latitudeLongitudeInterfacePort;

    @Override
    public AutoCompleteResponse execute(String inputText, String sessionToken) {

        MDC.put("operation", "buscarLocal");
        try{
            log.info("Iniciando busca de local. inputText={}", inputText);
            AutoCompleteResponse resultado = null;
            log.info("Busca de local concluida. inputText={}", inputText);
            return resultado;

        } catch (Exception e) {
            throw new BusinessException("Ocorreu um erro ao buscar o local", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            MDC.remove("operation");
        }
    }
}
