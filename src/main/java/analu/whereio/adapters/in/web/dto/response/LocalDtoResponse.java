package analu.whereio.adapters.in.web.dto.response;

import analu.whereio.application.model.Coordenadas;
import analu.whereio.application.model.Endereco;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter

@JsonPropertyOrder({"id", "nome", "endereco", "latitude", "longitude", "visitas"})
public class LocalDtoResponse {

    private String id;
    private String nome;
    private Endereco endereco;
    private Coordenadas coordenadas;
    private List<VisitaDtoResponse> visitas = new ArrayList<>();
}
