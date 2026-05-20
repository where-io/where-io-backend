package analu.whereio.application.ports.in.amigos;

import analu.whereio.application.model.Friendship;

public interface EnviarConviteAmizadeUsecase {

    /** Resolve o destinatário pelo nome de usuário público cadastrado em conta */
    Friendship execute(String requesterUserId, String nomeUsuarioDestinatario);
}
