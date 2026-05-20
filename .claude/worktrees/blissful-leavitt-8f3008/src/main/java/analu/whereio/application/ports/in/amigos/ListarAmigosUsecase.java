package analu.whereio.application.ports.in.amigos;

import analu.whereio.application.model.UserAccount;

import java.util.List;

public interface ListarAmigosUsecase {

    List<UserAccount> execute(String userId);
}
