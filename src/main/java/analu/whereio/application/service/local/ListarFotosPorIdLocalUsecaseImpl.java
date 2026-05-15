package analu.whereio.application.service.local;

import analu.whereio.application.model.Local;
import analu.whereio.application.ports.in.local.ListarFotosPorIdLocalUsecase;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ListarFotosPorIdLocalUsecaseImpl implements ListarFotosPorIdLocalUsecase {

    private static final Logger log = LoggerFactory.getLogger(ListarFotosPorIdLocalUsecaseImpl.class);

    private final LocalRepositoryPort localRepositoryPort;

    @Override
    public List<String> execute(String idLocal, String ownerUserId) {
        MDC.put("operation", "listarFotosLocal");
        try {
            if (idLocal == null || idLocal.isBlank()) {
                throw new BusinessException("Identificador do local é obrigatório", HttpStatus.BAD_REQUEST);
            }

            Local local = localRepositoryPort.buscarPorIdLocal(idLocal);
            if (local == null) {
                throw new BusinessException("Local não encontrado", HttpStatus.NOT_FOUND);
            }
            if (local.getOwnerUserId() == null || !local.getOwnerUserId().equals(ownerUserId)) {
                throw new BusinessException("Acesso negado a este local", HttpStatus.FORBIDDEN);
            }

            if (local.getFotos() == null || local.getFotos().isEmpty()) {
                return List.of();
            }

            log.debug("Listagem de fotos do local. idLocal={} quantidade={}", idLocal, local.getFotos().size());
            return Collections.unmodifiableList(local.getFotos());
        } finally {
            MDC.remove("operation");
        }
    }
}
