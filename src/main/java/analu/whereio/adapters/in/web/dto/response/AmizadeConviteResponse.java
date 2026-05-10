package analu.whereio.adapters.in.web.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class AmizadeConviteResponse {

    private String id;
    private String solicitanteId;
    private String destinatarioId;
    private String status;
    private Instant criadoEm;
    private Instant aceitoEm;
}
