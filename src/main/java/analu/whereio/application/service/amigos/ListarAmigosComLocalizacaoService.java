package analu.whereio.application.service.amigos;

import analu.whereio.application.model.SharingSettings;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.amigos.ListarAmigosComLocalizacaoUsecase;
import analu.whereio.application.ports.out.SharingSettingsRepositoryPort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ListarAmigosComLocalizacaoService implements ListarAmigosComLocalizacaoUsecase {

    private final SharingSettingsRepositoryPort sharingSettingsRepositoryPort;
    private final UserAccountRepositoryPort userAccountRepositoryPort;

    @Override
    public List<UserAccount> execute(String userId) {
        MDC.put("operation", "listarAmigosComLocalizacao");
        try {
            Set<String> toUserIds = sharingSettingsRepositoryPort
                    .findByFromUserIdAndShareLocationTrue(userId)
                    .stream()
                    .map(SharingSettings::getToUserId)
                    .collect(Collectors.toSet());
            return userAccountRepositoryPort.findAllById(toUserIds);
        } finally {
            MDC.remove("operation");
        }
    }
}
