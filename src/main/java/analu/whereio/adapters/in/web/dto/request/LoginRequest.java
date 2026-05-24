package analu.whereio.adapters.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    /** Email ou @nomeUsuario */
    @NotBlank
    private String identifier;

    @NotBlank
    private String password;
}
