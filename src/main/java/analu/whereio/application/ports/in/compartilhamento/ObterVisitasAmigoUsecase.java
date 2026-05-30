package analu.whereio.application.ports.in.compartilhamento;

import analu.whereio.application.model.Visita;

import java.util.List;

public interface ObterVisitasAmigoUsecase {

    List<Visita> execute(String viewerId, String friendId);
}
