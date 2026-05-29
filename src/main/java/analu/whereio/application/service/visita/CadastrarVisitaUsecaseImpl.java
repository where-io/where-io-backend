package analu.whereio.application.service.visita;

import analu.whereio.application.model.Visita;
import analu.whereio.application.ports.in.visita.CadastrarVisitaUsecase;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.application.ports.out.VisitaRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class CadastrarVisitaUsecaseImpl implements CadastrarVisitaUsecase {

    private final LocalRepositoryPort localRepositoryPort;
    private final VisitaRepositoryPort visitaRepositoryPort;

    @Override
    public String execute(Visita visita) {

        if (visita.getUserId() == null || visita.getUserId().isBlank()) {
            throw new BusinessException("Usuário da visita é obrigatório", HttpStatus.BAD_REQUEST);
        }

        var local = localRepositoryPort.buscarPorIdLocal(visita.getIdLocal());
        if (isNull(local) || local.getOwnerUserId() == null || !local.getOwnerUserId().equals(visita.getUserId())) {
            throw new BusinessException("Não existe restaurante com esse id", HttpStatus.NOT_FOUND);
        }

        if (!Boolean.TRUE.equals(local.getVisitacao())) {
            throw new BusinessException("Este local não permite registro de visitas", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        try {
            return visitaRepositoryPort.adicionarVisita(visita);
        } catch (Exception e) {
            throw new BusinessException("Ocorreu um erro ao cadastrar a visita", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
