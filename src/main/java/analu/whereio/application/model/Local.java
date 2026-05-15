package analu.whereio.application.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Local {

    private String id;
    /** ID do usuário que cadastrou o local (Mongo {@code user_table}). */
    private String ownerUserId;
    private String nome;
    private Endereco endereco;
    private Coordenadas coordenadas;
    private List<String> idTags = new ArrayList<>();
    private List<Categoria> tags = new ArrayList<>();
    private List<Visita> visitas = new ArrayList<>();
    /** URL absoluta da capa do local (ex.: {@code http://localhost:8080/media/uuid.jpg}). */
    private String imagemUrl;
    /** Nomes de arquivo em disco (galeria do local; servidos em {@code /media/{nome}}). */
    private List<String> fotos = new ArrayList<>();
}
