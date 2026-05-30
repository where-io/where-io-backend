package analu.whereio.application.service.compartilhamento;

import analu.whereio.application.model.Local;
import analu.whereio.application.ports.in.compartilhamento.ObterLocaisAmigoUsecase;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ObterLocaisAmigoService implements ObterLocaisAmigoUsecase {

    private final SharingPermissaoService sharingPermissaoService;
    private final LocalRepositoryPort localRepositoryPort;

    @Override
    public List<Local> execute(String viewerId, String friendId) {
        MDC.put("operation", "obterLocaisAmigo");
        try {
            if (!sharingPermissaoService.podeVerLocais(viewerId, friendId)) {
                throw new BusinessException("Este usuário não compartilhou seus locais com você", HttpStatus.FORBIDDEN);
            }
            return localRepositoryPort.buscarTodosLocalPorUsuario(friendId);
        } finally {
            MDC.remove("operation");
        }
    }
}
