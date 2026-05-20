package analu.whereio.application.service.local;

import analu.whereio.adapters.out.external.geocoding.record.AutoCompleteResponse;
import analu.whereio.application.ports.in.local.BuscarLocalUsecase;
import analu.whereio.application.ports.out.PlacesApiPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor

public class BuscarLocalUsecaseImpl implements BuscarLocalUsecase {

    private static final Logger log = LoggerFactory.getLogger(BuscarLocalUsecaseImpl.class);

    private final PlacesApiPort placesApiPort;

    @Override
    public AutoCompleteResponse execute(String inputText, String sessionToken) {

        MDC.put("operation", "buscarLocal");
        try{
            log.info("Iniciando busca de local. inputText={}", inputText);
            if (inputText == null || inputText.trim().length() <= 3) {
                return new AutoCompleteResponse(List.of());
            }
            AutoCompleteResponse resultado = placesApiPort.placeAutocomplete(inputText.trim(), sessionToken);
            log.info("Busca de local concluida. inputText={}, sugestoes={}", inputText,
                    resultado != null && resultado.suggestions() != null ? resultado.suggestions().size() : 0);
            return resultado;

        } catch (Exception e) {
            log.error("Erro ao buscar autocomplete de local. inputText={} erro={}", inputText, e.getMessage(), e);
            return new AutoCompleteResponse(List.of());
        } finally {
            MDC.remove("operation");
        }
    }
}
