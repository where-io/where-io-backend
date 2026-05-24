package analu.whereio.application.ports.in.usuario;

import analu.whereio.application.model.UserAccount;

public interface ObterPerfilUsuarioUsecase {
    UserAccount execute(String userId);
}
