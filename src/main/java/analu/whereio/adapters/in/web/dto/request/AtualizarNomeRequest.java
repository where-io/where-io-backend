package analu.whereio.adapters.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtualizarNomeRequest {

    @NotBlank(message = "nome é obrigatório")
    @Size(min = 2, max = 80, message = "nome deve ter entre 2 e 80 caracteres")
    private String nome;
}
