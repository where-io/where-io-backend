package analu.whereio.application.service.visita;

import analu.whereio.adapters.in.web.converter.VisitaConverter;
import analu.whereio.adapters.in.web.dto.response.VisitaDtoResponse;
import analu.whereio.application.ports.in.visita.BuscarVisitaPorIdLocalUsecase;
import analu.whereio.application.ports.out.VisitaRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BuscarVisitaPorIdLocalUsecaseImpl implements BuscarVisitaPorIdLocalUsecase {

    private static final Logger log = LoggerFactory.getLogger(BuscarVisitaPorIdLocalUsecaseImpl.class);

    private final VisitaRepositoryPort visitaRepositoryPort;
    private final VisitaConverter visitaConverter;

    @Override
    public List<VisitaDtoResponse> execute(String idLocal) {

        MDC.put("operation", "buscarVisitas");
        try{
            log.info("Iniciando busca de visitas por idLocal. idLocal={}", idLocal);
            List<VisitaDtoResponse> lista = visitaRepositoryPort
                    .buscarVisitasPorIdLocal(idLocal)
                    .stream()
                    .map(visitaConverter::toResponse)
                    .toList();

            log.info("Busca de visitas concluida. idLocal={} quantidade={}", idLocal, lista.size());
            return lista;

        }catch (Exception e){
            throw new BusinessException("Ocorreu um erro ao buscar as visitas por id do local", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            MDC.remove("operation");
        }
    }
}
