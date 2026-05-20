package analu.whereio.application.ports.in.visita;

import analu.whereio.application.model.Visita;

public interface AtualizarVisitaUsecase {

    void execute(String idVisita, Visita visita, String userId);
}
