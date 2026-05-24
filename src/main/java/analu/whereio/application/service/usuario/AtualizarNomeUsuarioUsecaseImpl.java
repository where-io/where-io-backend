package analu.whereio.application.service.usuario;

import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.usuario.AtualizarNomeUsuarioUsecase;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AtualizarNomeUsuarioUsecaseImpl implements AtualizarNomeUsuarioUsecase {

    private final UserAccountRepositoryPort userAccountRepositoryPort;

    @Override
    public UserAccount execute(String userId, String novoNome) {
        MDC.put("operation", "atualizarNomeUsuario");
        MDC.put("entityId", userId);
        try {
            UserAccount account = userAccountRepositoryPort.findById(userId)
                    .orElseThrow(() -> new BusinessException("Usuário não encontrado", HttpStatus.NOT_FOUND));
            account.setNome(novoNome.trim());
            return userAccountRepositoryPort.save(account);
        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
