package analu.whereio.application.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserAccount {

    private String id;
    private String email;
    private String encodedPassword;
    private String nome;
    /** Identificador público único (minúsculas), usado para convites de amizade */
    private String nomeUsuario;
    private List<String> roles = new ArrayList<>(List.of("USER"));
    private Instant createdAt;
}
