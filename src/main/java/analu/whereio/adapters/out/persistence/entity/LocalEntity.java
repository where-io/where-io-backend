package analu.whereio.adapters.out.persistence.entity;

import analu.whereio.application.model.Coordenadas;
import analu.whereio.application.model.Endereco;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "local_table")
public class LocalEntity {

    @Id
    private String id;
    private String nome;
    private Endereco endereco;
    private Coordenadas coordenadas;
}