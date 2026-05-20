package analu.whereio.adapters.in.web.dto.response;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter; import lombok.Setter;
@Getter @Setter
@JsonPropertyOrder({"id", "nome", "cor"})
public class TagDtoResponse {
    private String id;
    private String nome;
    private String cor;
}
