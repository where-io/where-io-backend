package analu.whereio.application.service.local;

import analu.whereio.application.model.Local;
import analu.whereio.application.ports.in.local.RemoverLocalUsecase;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoverLocalUsecaseImpl implements RemoverLocalUsecase {

    private static final Logger log = LoggerFactory.getLogger(RemoverLocalUsecaseImpl.class);

    private final LocalRepositoryPort localRepositoryPort;

    @Override
    public void execute(String id, String ownerUserId) {

        MDC.put("operation", "removerLocal");
        try{
            log.info("Iniciando remocao de local. id={}", id);
            Local local = localRepositoryPort.buscarPorIdLocal(id);
            if (local == null || local.getOwnerUserId() == null || !local.getOwnerUserId().equals(ownerUserId)) {
                throw new BusinessException("Local não encontrado", HttpStatus.NOT_FOUND);
            }
            localRepositoryPort.removerLocalPorId(id);
            log.info("Local removido com sucesso. id={}", id);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Ocorreu um erro ao remover o local", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            MDC.remove("operation");
        }
    }
}
