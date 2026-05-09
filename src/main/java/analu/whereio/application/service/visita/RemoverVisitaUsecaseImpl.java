package analu.whereio.application.service.visita;

import analu.whereio.application.ports.in.visita.RemoverVisitaUsecase;
import analu.whereio.application.ports.out.VisitaRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoverVisitaUsecaseImpl implements RemoverVisitaUsecase {

    private static final Logger log = LoggerFactory.getLogger(RemoverVisitaUsecaseImpl.class);

    private final VisitaRepositoryPort visitaRepositoryPort;

    @Override
    public void execute(String id) {

        MDC.put("operation", "removerVisita");
        try{
            log.info("Iniciando remocao de visita. id={}", id);
            visitaRepositoryPort.removerVisita(id);
            log.info("Visita removida com sucesso. id={}", id);

        }catch (Exception e){
            throw new BusinessException("Erro ao remover visita: ", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            MDC.remove("operation");
        }
    }
}
