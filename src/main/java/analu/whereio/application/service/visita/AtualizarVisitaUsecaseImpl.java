package analu.whereio.application.service.visita;

import analu.whereio.application.model.Visita;
import analu.whereio.application.ports.in.visita.AtualizarVisitaUsecase;
import analu.whereio.application.ports.out.VisitaRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class AtualizarVisitaUsecaseImpl implements AtualizarVisitaUsecase {

    private static final Logger log = LoggerFactory.getLogger(AtualizarVisitaUsecaseImpl.class);

    private final VisitaRepositoryPort visitaRepositoryPort;

    @Override
    public void execute(String idVisita, Visita visita, String userId) {

        MDC.put("operation", "atualizarVisita");
        try {
            log.info("Iniciando atualizacao de visita. id={}", idVisita);

            Visita existente = visitaRepositoryPort.buscarPorId(idVisita);
            if (isNull(existente) || existente.getUserId() == null || !existente.getUserId().equals(userId)) {
                log.warn("Visita nao encontrada para atualizacao. id={}", idVisita);
                throw new BusinessException("Visita não encontrada", HttpStatus.NOT_FOUND);
            }

            try{
                visita.setId(idVisita);
                visita.setUserId(existente.getUserId());
                visitaRepositoryPort.atualizarVisita(visita);

            }catch (Exception e){
                throw new BusinessException("Ocorreu um erro ao atualizar a visita", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            log.info("Visita atualizada com sucesso. id={}", idVisita);

        } finally {
            MDC.remove("operation");
        }
    }
}
