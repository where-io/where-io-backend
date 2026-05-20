package analu.whereio.adapters.in.web.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class VisitaDtoResponse {

    private String id;
    private LocalDate dataVisita;
    private int avaliacao;
    private String comentario;
    private String idLocal;
}
