package analu.whereio.application.service.usuario;

import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.usuario.AtualizarFotoPerfilUsecase;
import analu.whereio.application.ports.out.FileStoragePort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AtualizarFotoPerfilUsecaseImpl implements AtualizarFotoPerfilUsecase {

    private static final Logger log = LoggerFactory.getLogger(AtualizarFotoPerfilUsecaseImpl.class);

    private final FileStoragePort fileStoragePort;
    private final UserAccountRepositoryPort userAccountRepositoryPort;

    @Override
    public String execute(MultipartFile file, String userId) {
        MDC.put("operation", "atualizarFotoPerfil");
        MDC.put("entityId", userId);
        try {
            if (file == null || file.isEmpty()) {
                throw new BusinessException("Arquivo é obrigatório", HttpStatus.BAD_REQUEST);
            }

            UserAccount account = userAccountRepositoryPort.findById(userId)
                    .orElseThrow(() -> new BusinessException("Usuário não encontrado", HttpStatus.NOT_FOUND));

            String novaKey;
            try {
                novaKey = fileStoragePort.salvar(file);
            } catch (IOException e) {
                log.warn("Falha ao salvar foto de perfil. userId={}", userId, e);
                throw new BusinessException("Erro ao salvar arquivo", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (account.getFotoPerfil() != null) {
                try {
                    fileStoragePort.deletar(account.getFotoPerfil());
                } catch (IOException e) {
                    log.warn("Falha ao deletar foto antiga do perfil. userId={}", userId, e);
                }
            }

            account.setFotoPerfil(novaKey);
            userAccountRepositoryPort.save(account);
            return novaKey;
        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
