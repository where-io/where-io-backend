package analu.whereio.application.service.compartilhamento;

import analu.whereio.application.model.SharingSettings;
import analu.whereio.application.ports.out.SharingSettingsRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SharingPermissaoService {

    private final SharingSettingsRepositoryPort sharingSettingsRepositoryPort;

    public boolean podeVerLocais(String viewerId, String ownerId) {
        return sharingSettingsRepositoryPort
                .findByFromUserIdAndToUserId(ownerId, viewerId)
                .map(SharingSettings::isSharePlaces)
                .orElse(false);
    }

    public boolean podeVerVisitas(String viewerId, String ownerId) {
        return sharingSettingsRepositoryPort
                .findByFromUserIdAndToUserId(ownerId, viewerId)
                .map(SharingSettings::isShareVisits)
                .orElse(false);
    }
}
