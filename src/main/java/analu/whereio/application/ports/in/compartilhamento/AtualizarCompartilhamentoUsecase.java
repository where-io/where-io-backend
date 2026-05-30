package analu.whereio.application.ports.in.compartilhamento;

import analu.whereio.application.model.SharingSettings;

public interface AtualizarCompartilhamentoUsecase {

    SharingSettings execute(String requesterId, String friendId, boolean shareLocation, boolean sharePlaces, boolean shareVisits);
}
