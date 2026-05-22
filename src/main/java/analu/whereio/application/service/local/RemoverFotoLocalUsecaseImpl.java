package analu.whereio.application.service.local;

import analu.whereio.application.model.Local;
import analu.whereio.application.ports.in.local.RemoverFotoLocalUsecase;
import analu.whereio.application.ports.out.FileStoragePort;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RemoverFotoLocalUsecaseImpl implements RemoverFotoLocalUsecase {

    private static final Logger log = LoggerFactory.getLogger(RemoverFotoLocalUsecaseImpl.class);

    private final FileStoragePort fileStoragePort;
    private final LocalRepositoryPort localRepositoryPort;

    @Override
    public void execute(String idLocal, String fileName, String ownerUserId) {
        MDC.put("operation", "removerFotoLocal");
        try {
            if (idLocal == null || idLocal.isBlank()) {
                throw new BusinessException("Identificador do local é obrigatório", HttpStatus.BAD_REQUEST);
            }
            if (fileName == null || fileName.isBlank()) {
                throw new BusinessException("Nome do arquivo é obrigatório", HttpStatus.BAD_REQUEST);
            }

            Local local = localRepositoryPort.buscarPorIdLocal(idLocal);
            if (local == null) {
                throw new BusinessException("Local não encontrado", HttpStatus.NOT_FOUND);
            }
            if (local.getOwnerUserId() == null || !local.getOwnerUserId().equals(ownerUserId)) {
                throw new BusinessException("Acesso negado a este local", HttpStatus.FORBIDDEN);
            }

            List<String> fotos = local.getFotos();
            if (fotos == null || fotos.isEmpty()) {
                throw new BusinessException("Nenhuma foto neste local", HttpStatus.NOT_FOUND);
            }
            boolean removedFromList = fotos.removeIf(fileName::equals);
            if (!removedFromList) {
                throw new BusinessException("Foto não encontrada neste local", HttpStatus.NOT_FOUND);
            }

            local.setFotos(new ArrayList<>(fotos));
            localRepositoryPort.atualizarLocal(local);

            try {
                fileStoragePort.deletar(fileName);
            } catch (IllegalArgumentException ignored) {
                throw new BusinessException("Nome de arquivo inválido", HttpStatus.BAD_REQUEST);
            } catch (IOException e) {
                log.warn("Arquivo removido da lista mas falhou ao apagar no disco. idLocal={} arquivo={}", idLocal, fileName, e);
                throw new BusinessException("Erro ao remover arquivo do disco", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            MDC.put("entityId", idLocal);
            log.info("Foto removida do local. idLocal={} arquivo={}", idLocal, fileName);
        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
