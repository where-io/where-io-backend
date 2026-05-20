package analu.whereio.application.ports.in.local;

import analu.whereio.application.model.Local;

import java.util.List;

public interface BuscarTodosLocalUsecase {

    List<Local> execute(String ownerUserId, int page, int size);
}
