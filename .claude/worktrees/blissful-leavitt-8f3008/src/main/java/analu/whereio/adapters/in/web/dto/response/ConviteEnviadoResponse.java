package analu.whereio.adapters.in.web.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ConviteEnviadoResponse {

    private String id;
    private String nome;
    private String status;
    private Instant criadoEm;
    private Instant aceitoEm;
}
