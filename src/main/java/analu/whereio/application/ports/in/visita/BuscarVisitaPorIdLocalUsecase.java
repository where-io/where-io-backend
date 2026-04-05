package analu.whereio.application.ports.in.visita;

import analu.whereio.adapters.in.web.dto.response.VisitaDtoResponse;

import java.util.List;

public interface BuscarVisitaPorIdLocalUsecase {

    List<VisitaDtoResponse> execute(String idLocal);
}
