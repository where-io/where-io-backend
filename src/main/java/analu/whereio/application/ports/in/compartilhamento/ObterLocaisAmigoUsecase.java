package analu.whereio.application.ports.in.compartilhamento;

import analu.whereio.application.model.Local;

import java.util.List;

public interface ObterLocaisAmigoUsecase {

    List<Local> execute(String viewerId, String friendId);
}
