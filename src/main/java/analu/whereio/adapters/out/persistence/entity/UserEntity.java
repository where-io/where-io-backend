package analu.whereio.adapters.out.persistence.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(collection = "user_table")
public class UserEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;
    private String nome;

    @Indexed(unique = true, sparse = true)
    private String nomeUsuario;
    private List<String> roles = new ArrayList<>(List.of("USER"));
    private Instant createdAt;
}
