package analu.whereio.application.ports.in.usuario;

import analu.whereio.application.model.UserAccount;

public interface AtualizarNomeUsuarioUsecase {
    UserAccount execute(String userId, String novoNome);
}
