package analu.whereio.application.service.local;

import analu.whereio.application.model.Local;
import analu.whereio.application.ports.in.local.AdicionarFotoLocalUsecase;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.application.service.files.FileStorageService;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class AdicionarFotoLocalUsecaseImpl implements AdicionarFotoLocalUsecase {

    private static final Logger log = LoggerFactory.getLogger(AdicionarFotoLocalUsecaseImpl.class);

    private final FileStorageService fileStorageService;
    private final LocalRepositoryPort localRepositoryPort;

    @Override
    public String execute(MultipartFile file, String idLocal, String ownerUserId) {
        MDC.put("operation", "adicionarFotoLocal");
        try {
            if (file == null || file.isEmpty()) {
                throw new BusinessException("Arquivo é obrigatório", HttpStatus.BAD_REQUEST);
            }
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

            String storedName;
            try {
                storedName = fileStorageService.saveFile(file);
            } catch (IOException e) {
                log.warn("Falha ao gravar arquivo do local. idLocal={}", idLocal, e);
                throw new BusinessException("Erro ao salvar arquivo", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (local.getFotos() == null) {
                local.setFotos(new ArrayList<>());
            }
            local.getFotos().add(storedName);

            localRepositoryPort.atualizarLocal(local);

            MDC.put("entityId", idLocal);
            log.info("Foto associada ao local. idLocal={} arquivo={}", idLocal, storedName);
            return storedName;
        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
