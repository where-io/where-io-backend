package analu.whereio.application.ports.in.amigos;

import analu.whereio.application.model.ConviteAmizadeResumoItem;

import java.util.List;

public interface ListarConvitesRecebidosAmizadeUsecase {

    List<ConviteAmizadeResumoItem> execute(String userId);
}
