package analu.whereio.application.ports.in.amigos;

import analu.whereio.application.model.ConviteAmizadeResumoItem;

import java.util.List;

public interface ListarConvitesEnviadosAmizadeUsecase {

    List<ConviteAmizadeResumoItem> execute(String userId);
}
