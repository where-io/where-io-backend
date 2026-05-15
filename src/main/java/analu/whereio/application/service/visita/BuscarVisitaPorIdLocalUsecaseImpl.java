package analu.whereio.application.service.visita;

import analu.whereio.adapters.in.web.converter.VisitaConverter;
import analu.whereio.adapters.in.web.dto.response.VisitaDtoResponse;
import analu.whereio.application.model.Local;
import analu.whereio.application.ports.in.visita.BuscarVisitaPorIdLocalUsecase;
import analu.whereio.application.ports.out.LocalRepositoryPort;
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
    private final LocalRepositoryPort localRepositoryPort;
    private final VisitaConverter visitaConverter;

    @Override
    public List<VisitaDtoResponse> execute(String idLocal, String userId) {

        MDC.put("operation", "buscarVisitas");
        try{
            log.info("Iniciando busca de visitas por idLocal. idLocal={}", idLocal);
            Local local = localRepositoryPort.buscarPorIdLocal(idLocal);
            if (local == null || local.getOwnerUserId() == null || !local.getOwnerUserId().equals(userId)) {
                throw new BusinessException("Local não encontrado", HttpStatus.NOT_FOUND);
            }
            List<VisitaDtoResponse> lista = visitaRepositoryPort
                    .buscarVisitasPorIdLocal(idLocal, userId)
                    .stream()
                    .map(visitaConverter::toResponse)
                    .toList();

            log.info("Busca de visitas concluida. idLocal={} quantidade={}", idLocal, lista.size());
            return lista;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Ocorreu um erro ao buscar as visitas por id do local", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            MDC.remove("operation");
        }
    }
}
