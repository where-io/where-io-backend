package analu.whereio.application.ports.in.local;

import java.util.List;

public interface ListarFotosPorIdLocalUsecase {

    /** Nomes de arquivo armazenados (iguais aos usados em {@code /media/{nome}}). */
    List<String> execute(String idLocal, String ownerUserId);
}
