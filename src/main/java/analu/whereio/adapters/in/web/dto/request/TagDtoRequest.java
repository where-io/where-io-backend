package analu.whereio.adapters.in.web.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.*;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TagDtoRequest {
    @NotNull
    private String nome;
    @NotNull
    private String cor;
}
