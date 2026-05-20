package analu.whereio.adapters.in.web.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LocalBuscarDtoResponse {

    private List<PredictionDto> predictions;
}


