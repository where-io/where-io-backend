package analu.whereio.application.service.visita;

import analu.whereio.application.model.Visita;
import analu.whereio.application.ports.in.visita.RemoverVisitaUsecase;
import analu.whereio.application.ports.out.VisitaRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoverVisitaUsecaseImpl implements RemoverVisitaUsecase {

    private final VisitaRepositoryPort visitaRepositoryPort;

    @Override
    public void execute(String id, String userId) {
        try {
            Visita existente = visitaRepositoryPort.buscarPorId(id);
            if (existente == null || existente.getUserId() == null || !existente.getUserId().equals(userId)) {
                throw new BusinessException("Visita não encontrada", HttpStatus.NOT_FOUND);
            }
            visitaRepositoryPort.removerVisita(id);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Erro ao remover visita: ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
