package analu.whereio.application.service.amigos;

import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.amigos.ListarAmigosUsecase;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ListarAmigosUsecaseImpl implements ListarAmigosUsecase {

    private final FriendshipRepositoryPort friendshipRepositoryPort;
    private final UserAccountRepositoryPort userAccountRepositoryPort;

    @Override
    public List<UserAccount> execute(String userId) {
        MDC.put("operation", "listarAmigos");
        try {
            List<Friendship> amizades = friendshipRepositoryPort.findAcceptedForUser(userId);
            Set<String> outrosIds = new LinkedHashSet<>();
            for (Friendship f : amizades) {
                if (userId.equals(f.getRequesterUserId())) {
                    outrosIds.add(f.getAddresseeUserId());
                } else {
                    outrosIds.add(f.getRequesterUserId());
                }
            }

            List<UserAccount> resultado = new ArrayList<>();
            for (String idAmigo : outrosIds) {
                userAccountRepositoryPort.findById(idAmigo).ifPresent(resultado::add);
            }
            return resultado;
        } finally {
            MDC.remove("operation");
        }
    }
}
