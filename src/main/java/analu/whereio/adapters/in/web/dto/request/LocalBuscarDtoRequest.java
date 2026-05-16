package analu.whereio.adapters.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalBuscarDtoRequest {

    @NotBlank private String inputText;

    /** Opcional; usado para agrupar cobrança na sessão de autocomplete do Places. */
    private String sessionToken;

}
