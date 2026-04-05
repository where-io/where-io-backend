package analu.whereio.application.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Endereco {

    private String logradouro;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private String pais;

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s, %s, %s", logradouro, bairro, cidade, estado, cep, pais);
    }
}
