package analu.whereio.adapters.in.web.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioPerfilResponse {

    private String id;
    private String nome;
    private String nomeUsuario;
    private String email;
    private String fotoPerfilUrl;
}
