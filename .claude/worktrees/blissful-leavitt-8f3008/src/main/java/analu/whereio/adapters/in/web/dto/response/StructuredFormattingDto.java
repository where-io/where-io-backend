package analu.whereio.adapters.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StructuredFormattingDto {

    @JsonProperty("main_text")
    private String mainText;

    @JsonProperty("secondary_text")
    private String secondaryText;
}
