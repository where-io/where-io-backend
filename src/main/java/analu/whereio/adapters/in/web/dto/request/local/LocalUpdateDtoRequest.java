package analu.whereio.adapters.in.web.dto.request.local;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalUpdateDtoRequest {

    @NotNull
    private String nome;
    private Boolean visitacao;
    /** Opcional. Omitir para manter; string vazia remove a imagem. */
    private String imagemUrl;

    @Override
    public String toString() {
        return "LocalUpdateDtoRequest{" +
                "nome='" + nome + '\'' +
                ", visitacao=" + visitacao +
                ", imagemUrl='" + imagemUrl + '\'' +
                '}';
    }
}
