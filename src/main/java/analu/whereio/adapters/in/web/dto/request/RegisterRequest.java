package analu.whereio.adapters.in.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    private String password;

    private String nome;

    @NotBlank(message = "nomeUsuario é obrigatório")
    @Pattern(regexp = "^[a-z0-9_]{3,20}$",
             message = "nomeUsuario deve ter 3–20 caracteres: apenas letras minúsculas, números e _")
    private String nomeUsuario;
}
