package analu.whereio.adapters.in.web.dto.request;

import analu.whereio.application.model.Coordenadas;
import analu.whereio.application.model.Endereco;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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

}
