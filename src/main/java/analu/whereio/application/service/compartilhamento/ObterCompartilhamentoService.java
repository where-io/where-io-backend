package analu.whereio.application.service.compartilhamento;

import analu.whereio.application.model.SharingSettings;
import analu.whereio.application.ports.in.compartilhamento.ObterCompartilhamentoUsecase;
import analu.whereio.application.ports.out.SharingSettingsRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ObterCompartilhamentoService implements ObterCompartilhamentoUsecase {

    private final SharingSettingsRepositoryPort sharingSettingsRepositoryPort;

    @Override
    public SharingSettings execute(String requesterId, String friendId) {
        MDC.put("operation", "obterCompartilhamento");
        try {
            return sharingSettingsRepositoryPort
                    .findByFromUserIdAndToUserId(requesterId, friendId)
                    .orElseGet(() -> buildDefaults(requesterId, friendId));
        } finally {
            MDC.remove("operation");
        }
    }

    private SharingSettings buildDefaults(String fromUserId, String toUserId) {
        SharingSettings defaults = new SharingSettings();
        defaults.setFromUserId(fromUserId);
        defaults.setToUserId(toUserId);
        defaults.setShareLocation(true);
        defaults.setSharePlaces(false);
        defaults.setShareVisits(false);
        return defaults;
    }
}
