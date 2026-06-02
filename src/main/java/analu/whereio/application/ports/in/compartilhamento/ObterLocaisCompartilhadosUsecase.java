package analu.whereio.application.ports.in.compartilhamento;

import analu.whereio.application.model.FriendSharedLocal;

import java.util.List;

public interface ObterLocaisCompartilhadosUsecase {

    List<FriendSharedLocal> execute(String viewerId);
}
