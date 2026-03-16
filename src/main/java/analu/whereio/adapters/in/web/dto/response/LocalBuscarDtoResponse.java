package analu.whereio.adapters.in.web.dto.response;

import analu.whereio.adapters.out.external.geocoding.record.Suggestion;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LocalBuscarDtoResponse {
    private List<Suggestion> suggestions;
}
