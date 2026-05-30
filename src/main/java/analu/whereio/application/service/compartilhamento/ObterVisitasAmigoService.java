package analu.whereio.application.service.compartilhamento;

import analu.whereio.application.model.Visita;
import analu.whereio.application.ports.in.compartilhamento.ObterVisitasAmigoUsecase;
import analu.whereio.application.ports.out.VisitaRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ObterVisitasAmigoService implements ObterVisitasAmigoUsecase {

    private final SharingPermissaoService sharingPermissaoService;
    private final VisitaRepositoryPort visitaRepositoryPort;

    @Override
    public List<Visita> execute(String viewerId, String friendId) {
        MDC.put("operation", "obterVisitasAmigo");
        try {
            if (!sharingPermissaoService.podeVerVisitas(viewerId, friendId)) {
                throw new BusinessException("Este usuário não compartilhou suas visitas com você", HttpStatus.FORBIDDEN);
            }
            return visitaRepositoryPort.buscarTodasVisitasPorUsuario(friendId);
        } finally {
            MDC.remove("operation");
        }
    }
}
