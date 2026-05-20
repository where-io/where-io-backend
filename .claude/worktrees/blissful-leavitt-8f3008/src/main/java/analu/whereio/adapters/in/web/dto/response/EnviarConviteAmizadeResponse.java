package analu.whereio.adapters.in.web.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnviarConviteAmizadeResponse {

    private boolean sucesso;
    private String mensagem;
    private AmizadeConviteResponse convite;
}
