package analu.whereio.adapters.in.web.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtualizarCompartilhamentoRequest {

    private boolean shareLocation;
    private boolean sharePlaces;
    private boolean shareVisits;
}
