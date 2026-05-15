package analu.whereio.application.service.tag;

import analu.whereio.application.model.Local;
import analu.whereio.application.ports.in.tag.RemoverAssociacaoTagLocalUsecase;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RemoverAssociacaoTagLocalUsecaseImpl implements RemoverAssociacaoTagLocalUsecase {
    private static final Logger log = LoggerFactory.getLogger(RemoverAssociacaoTagLocalUsecaseImpl.class);
    private final LocalRepositoryPort localRepositoryPort;

    @Override
    public void execute(String idLocal, String idTag, String userId) {
        MDC.put("operation", "removerAssociacaoTagLocal");
        try {
            log.info("Iniciando remoção de associação. idLocal={} idTag={}", idLocal, idTag);
            Local local = localRepositoryPort.buscarPorIdLocal(idLocal);
            if (local == null || local.getOwnerUserId() == null || !local.getOwnerUserId().equals(userId)) {
                log.warn("Local não encontrado para remoção de associação. idLocal={}", idLocal);
                throw new BusinessException("Local não encontrado", HttpStatus.NOT_FOUND);
            }
            List<String> idTags = local.getIdTags();
            if (idTags == null || !idTags.contains(idTag)) {
                log.warn("Associação não encontrada. idLocal={} idTag={}", idLocal, idTag);
                throw new BusinessException("Associação entre local e tag não encontrada", HttpStatus.NOT_FOUND);
            }
            try {
                idTags.remove(idTag);
                localRepositoryPort.atualizarLocal(local);
                log.info("Associação removida com sucesso. idLocal={} idTag={}", idLocal, idTag);
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                throw new BusinessException("Ocorreu um erro ao remover a associação", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } finally {
            MDC.remove("operation");
        }
    }
}
