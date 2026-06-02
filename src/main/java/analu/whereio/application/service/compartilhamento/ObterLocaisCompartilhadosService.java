package analu.whereio.application.service.compartilhamento;

import analu.whereio.application.model.FriendSharedLocal;
import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.Local;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.compartilhamento.ObterLocaisCompartilhadosUsecase;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ObterLocaisCompartilhadosService implements ObterLocaisCompartilhadosUsecase {

    private final FriendshipRepositoryPort friendshipRepositoryPort;
    private final UserAccountRepositoryPort userAccountRepositoryPort;
    private final SharingPermissaoService sharingPermissaoService;
    private final LocalRepositoryPort localRepositoryPort;

    @Override
    public List<FriendSharedLocal> execute(String viewerId) {
        MDC.put("operation", "obterLocaisCompartilhados");
        try {
            Set<String> friendIds = obterIdsAmigos(viewerId);
            if (friendIds.isEmpty()) {
                return List.of();
            }

            Map<String, String> friendNames = userAccountRepositoryPort.findAllById(friendIds).stream()
                    .collect(Collectors.toMap(UserAccount::getId, UserAccount::getNome, (a, b) -> a));

            List<FriendSharedLocal> result = new ArrayList<>();
            for (String friendId : friendIds) {
                if (!sharingPermissaoService.podeVerLocais(viewerId, friendId)) {
                    continue;
                }
                List<Local> locais = localRepositoryPort.buscarTodosLocalPorUsuario(friendId);
                String ownerName = friendNames.getOrDefault(friendId, "");
                for (Local local : locais) {
                    result.add(new FriendSharedLocal(local, friendId, ownerName));
                }
            }
            return result;
        } finally {
            MDC.remove("operation");
        }
    }

    private Set<String> obterIdsAmigos(String viewerId) {
        List<Friendship> amizades = friendshipRepositoryPort.findAcceptedForUser(viewerId);
        Set<String> friendIds = new LinkedHashSet<>();
        for (Friendship amizade : amizades) {
            if (viewerId.equals(amizade.getRequesterUserId())) {
                friendIds.add(amizade.getAddresseeUserId());
            } else {
                friendIds.add(amizade.getRequesterUserId());
            }
        }
        return friendIds;
    }
}
