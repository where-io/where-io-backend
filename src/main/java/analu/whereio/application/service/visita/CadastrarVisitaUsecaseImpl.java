package analu.whereio.application.service.visita;

import analu.whereio.application.model.Visita;
import analu.whereio.application.ports.in.visita.CadastrarVisitaUsecase;
import analu.whereio.application.ports.out.LocalRepositoryPort;
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
public class CadastrarVisitaUsecaseImpl implements CadastrarVisitaUsecase {

    private static final Logger log = LoggerFactory.getLogger(CadastrarVisitaUsecaseImpl.class);

    private final LocalRepositoryPort localRepositoryPort;
    private final VisitaRepositoryPort visitaRepositoryPort;

    @Override
    public String execute(Visita visita) {

        MDC.put("operation", "cadastrarVisita");
        try {
            log.info("Iniciando cadastro de visita. idLocal={}", visita.getIdLocal());

            if(isNull(localRepositoryPort.buscarPorIdLocal(visita.getIdLocal()))){
                log.info("Validacao de existencia do local falhou. idLocal={}", visita.getIdLocal());
                throw new BusinessException("Não existe restaurante com esse id", HttpStatus.NOT_FOUND);
            }

            log.info("Local validado com sucesso. idLocal={}", visita.getIdLocal());

            try{
                String id = visitaRepositoryPort.adicionarVisita(visita);
                MDC.put("entityId", id);
                log.info("Visita cadastrada com sucesso. id={}", id);
                return id;

            } catch (Exception e) {
                throw new BusinessException("Ocorreu um erro ao cadastrar a visita", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
