package analu.whereio.adapters.in.web.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CompartilhamentoResponse {

    private String fromUserId;
    private String toUserId;
    private boolean shareLocation;
    private boolean sharePlaces;
    private boolean shareVisits;
    private Instant updatedAt;
}
