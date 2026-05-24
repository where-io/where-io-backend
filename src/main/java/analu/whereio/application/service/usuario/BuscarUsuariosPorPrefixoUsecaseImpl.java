package analu.whereio.application.service.usuario;

import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.usuario.BuscarUsuariosPorPrefixoUsecase;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BuscarUsuariosPorPrefixoUsecaseImpl implements BuscarUsuariosPorPrefixoUsecase {

    private static final int CANDIDATE_POOL = 20;
    private static final int MAX_RESULTS = 10;

    private final UserAccountRepositoryPort userAccountRepositoryPort;
    private final FriendshipRepositoryPort friendshipRepositoryPort;

    @Override
    public List<UserAccount> execute(String prefix, String requesterId) {
        MDC.put("operation", "buscarUsuariosPorPrefixo");
        try {
            String normalizedPrefix = prefix.trim().toLowerCase(Locale.ROOT);

            Set<String> confirmedFriendIds = friendshipRepositoryPort.findAcceptedForUser(requesterId)
                    .stream()
                    .map(f -> friendId(f, requesterId))
                    .collect(Collectors.toSet());

            return userAccountRepositoryPort.buscarPorPrefixoNomeUsuario(normalizedPrefix, CANDIDATE_POOL)
                    .stream()
                    .filter(u -> !u.getId().equals(requesterId))
                    .filter(u -> !confirmedFriendIds.contains(u.getId()))
                    .limit(MAX_RESULTS)
                    .toList();
        } finally {
            MDC.remove("operation");
        }
    }

    private String friendId(Friendship friendship, String requesterId) {
        return friendship.getRequesterUserId().equals(requesterId)
                ? friendship.getAddresseeUserId()
                : friendship.getRequesterUserId();
    }
}
