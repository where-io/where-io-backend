package analu.whereio.adapters.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PredictionDto {

    @JsonProperty("place_id")
    private String placeId;

    private String description;

    @JsonProperty("structured_formatting")
    private StructuredFormattingDto structuredFormatting;
}
