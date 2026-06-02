package analu.whereio.application.service.amigos;

import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.SharingSettings;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.amigos.ListarAmigosComLocalizacaoUsecase;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import analu.whereio.application.ports.out.SharingSettingsRepositoryPort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ListarAmigosComLocalizacaoService implements ListarAmigosComLocalizacaoUsecase {

    private final SharingSettingsRepositoryPort sharingSettingsRepositoryPort;
    private final FriendshipRepositoryPort friendshipRepositoryPort;
    private final UserAccountRepositoryPort userAccountRepositoryPort;

    @Override
    public List<UserAccount> execute(String userId) {
        MDC.put("operation", "listarAmigosComLocalizacao");
        try {
            // All accepted friend IDs
            Set<String> allFriendIds = friendshipRepositoryPort.findAcceptedForUser(userId).stream()
                    .map(f -> f.getRequesterUserId().equals(userId)
                            ? f.getAddresseeUserId()
                            : f.getRequesterUserId())
                    .collect(Collectors.toSet());

            // Build map of friendId -> shareLocation for friends that have explicit settings
            Map<String, Boolean> settingsMap = sharingSettingsRepositoryPort
                    .findByFromUserId(userId).stream()
                    .collect(Collectors.toMap(
                            SharingSettings::getToUserId,
                            SharingSettings::isShareLocation,
                            (a, b) -> a
                    ));

            // Include friend if: no settings document (default = true) OR shareLocation = true
            Set<String> targetIds = allFriendIds.stream()
                    .filter(id -> settingsMap.getOrDefault(id, true))
                    .collect(Collectors.toSet());

            return userAccountRepositoryPort.findAllById(targetIds);
        } finally {
            MDC.remove("operation");
        }
    }
}
