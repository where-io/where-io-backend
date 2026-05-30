package analu.whereio.application.ports.in.compartilhamento;

import analu.whereio.application.model.SharingSettings;

public interface ObterCompartilhamentoUsecase {

    SharingSettings execute(String requesterId, String friendId);
}
