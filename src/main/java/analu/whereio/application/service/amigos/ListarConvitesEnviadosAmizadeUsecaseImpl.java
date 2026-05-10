package analu.whereio.application.service.amigos;

import analu.whereio.application.model.ConviteAmizadeResumoItem;
import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.amigos.ListarConvitesEnviadosAmizadeUsecase;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListarConvitesEnviadosAmizadeUsecaseImpl implements ListarConvitesEnviadosAmizadeUsecase {

    private final FriendshipRepositoryPort friendshipRepositoryPort;
    private final UserAccountRepositoryPort userAccountRepositoryPort;

    @Override
    public List<ConviteAmizadeResumoItem> execute(String userId) {
        MDC.put("operation", "listarConvitesEnviadosAmizade");
        try {
            return friendshipRepositoryPort.findPendingSentByRequester(userId).stream()
                    .map(this::toItem)
                    .toList();
        } finally {
            MDC.remove("operation");
        }
    }

    private ConviteAmizadeResumoItem toItem(Friendship friendship) {
        ConviteAmizadeResumoItem item = new ConviteAmizadeResumoItem();
        item.setId(friendship.getId());
        item.setStatus(friendship.getStatus());
        item.setCreatedAt(friendship.getCreatedAt());
        item.setAcceptedAt(friendship.getAcceptedAt());
        item.setNome(userAccountRepositoryPort.findById(friendship.getAddresseeUserId())
                .map(this::nomeParaExibicao)
                .orElse("Usuário"));
        return item;
    }

    private String nomeParaExibicao(UserAccount account) {
        if (account.getNome() != null && !account.getNome().isBlank()) {
            return account.getNome().trim();
        }
        if (account.getNomeUsuario() != null && !account.getNomeUsuario().isBlank()) {
            return account.getNomeUsuario().trim();
        }
        return "Usuário";
    }
}
