package analu.whereio.application.ports.in.local;

import analu.whereio.application.model.Local;

public interface AtualizarLocalUsecase {

    void execute(Local local, String id, String ownerUserId);
}
