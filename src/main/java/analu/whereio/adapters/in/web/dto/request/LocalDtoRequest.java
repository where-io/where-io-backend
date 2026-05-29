package analu.whereio.adapters.in.web.dto.request;

import analu.whereio.application.model.Coordenadas;
import analu.whereio.application.model.Categoria;
import analu.whereio.application.model.Endereco;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalDtoRequest {

    @NotNull
    private String nome;
    @NotNull
    private Endereco endereco;

    private Coordenadas coordenadas;
    @Builder.Default
    private List<String> idTags = new ArrayList<>();
    @Builder.Default
    private List<Categoria> tags = new ArrayList<>();

    private Boolean visitacao;
    /** Opcional. Omitir para manter; string vazia remove a imagem. */
    private String imagemUrl;


    @Override
    public String toString() {
        return "LocalDtoRequest{" +
                "nome='" + nome + '\'' +
                ", endereco=" + endereco +
                ", coordenadas=" + coordenadas +
                ", idTags=" + idTags +
                ", tags=" + tags +
                ", visitacao=" + visitacao +
                ", imagemUrl='" + imagemUrl + '\'' +
                '}';
    }
}
