package analu.whereio.application.ports.in.usuario;

import analu.whereio.application.model.UserAccount;

import java.util.List;

public interface BuscarUsuariosPorPrefixoUsecase {
    List<UserAccount> execute(String prefix, String requesterId);
}
