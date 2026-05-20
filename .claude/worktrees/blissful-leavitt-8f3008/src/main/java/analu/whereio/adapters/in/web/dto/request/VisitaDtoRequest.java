package analu.whereio.adapters.in.web.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class VisitaDtoRequest {

    private LocalDate dataVisita;
    private int avaliacao;
    private String comentario;
    private String idLocal;
}
