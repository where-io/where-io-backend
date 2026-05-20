package analu.whereio.adapters.in.web.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceDetailsDtoResponse {

    private double lat;
    private double lng;
    private String logradouro;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private String pais;
    private String formattedAddress;
}
