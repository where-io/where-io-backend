package analu.whereio.adapters.in.web.dto.response;

import analu.whereio.application.model.Coordenadas;
import analu.whereio.application.model.Categoria;
import analu.whereio.application.model.Endereco;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter

@JsonPropertyOrder({"id", "nome", "visitacao", "endereco", "latitude", "longitude", "imagemUrl", "fotoUrls", "idTags", "tags", "visitas"})
public class LocalDtoResponse {

    private String id;
    private String nome;
    private Boolean visitacao;
    private Endereco endereco;
    private Coordenadas coordenadas;
    private String imagemUrl;
    private List<String> fotoUrls = new ArrayList<>();
    private List<String> idTags = new ArrayList<>();
    private List<Categoria> tags = new ArrayList<>();
    private List<VisitaDtoResponse> visitas = new ArrayList<>();
}
