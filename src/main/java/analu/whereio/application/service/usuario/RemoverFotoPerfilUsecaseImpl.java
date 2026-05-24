package analu.whereio.application.service.usuario;

import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.usuario.RemoverFotoPerfilUsecase;
import analu.whereio.application.ports.out.FileStoragePort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RemoverFotoPerfilUsecaseImpl implements RemoverFotoPerfilUsecase {

    private static final Logger log = LoggerFactory.getLogger(RemoverFotoPerfilUsecaseImpl.class);

    private final FileStoragePort fileStoragePort;
    private final UserAccountRepositoryPort userAccountRepositoryPort;

    @Override
    public void execute(String userId) {
        MDC.put("operation", "removerFotoPerfil");
        MDC.put("entityId", userId);
        try {
            UserAccount account = userAccountRepositoryPort.findById(userId)
                    .orElseThrow(() -> new BusinessException("Usuário não encontrado", HttpStatus.NOT_FOUND));

            if (account.getFotoPerfil() == null) {
                return;
            }

            try {
                fileStoragePort.deletar(account.getFotoPerfil());
            } catch (IOException e) {
                log.warn("Falha ao deletar foto de perfil. userId={}", userId, e);
            }

            account.setFotoPerfil(null);
            userAccountRepositoryPort.save(account);
        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
