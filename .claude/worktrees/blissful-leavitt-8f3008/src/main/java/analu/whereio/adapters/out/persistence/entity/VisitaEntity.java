package analu.whereio.adapters.out.persistence.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Getter
@Setter
@Document(collection = "visita_table")
public class VisitaEntity {

    @Id
    private String id;
    private String userId;
    private LocalDate dataVisita;
    private int avaliacao;
    private String comentario;
    private String idLocal;

}
