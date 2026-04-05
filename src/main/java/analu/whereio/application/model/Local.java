package analu.whereio.application.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Local {

    private String id;
    private String nome;
    private Endereco endereco;
    private Coordenadas coordenadas;
    private List<Visita> visitas = new ArrayList<>();
}
