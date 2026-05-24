package analu.whereio.application.service.usuario;

import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.usuario.ObterPerfilUsuarioUsecase;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ObterPerfilUsuarioUsecaseImpl implements ObterPerfilUsuarioUsecase {

    private final UserAccountRepositoryPort userAccountRepositoryPort;

    @Override
    public UserAccount execute(String userId) {
        MDC.put("operation", "obterPerfilUsuario");
        try {
            return userAccountRepositoryPort.findById(userId)
                    .orElseThrow(() -> new BusinessException("Usuário não encontrado", HttpStatus.NOT_FOUND));
        } finally {
            MDC.remove("operation");
        }
    }
}
