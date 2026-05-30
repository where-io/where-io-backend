package analu.whereio.application.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class SharingSettings {

    private String id;
    private String fromUserId;
    private String toUserId;
    private boolean shareLocation;
    private boolean sharePlaces;
    private boolean shareVisits;
    private Instant updatedAt;
}
