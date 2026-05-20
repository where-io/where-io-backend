package analu.whereio.application.model;

import lombok.Getter;
import lombok.Setter;


import java.time.LocalDate;

@Getter
@Setter
public class Visita {

    private String id;
    /** ID do usuário que registrou a visita. */
    private String userId;
    private LocalDate dataVisita;
    private int avaliacao;
    private String comentario;
    private String idLocal;

}
