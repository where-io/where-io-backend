package analu.whereio.adapters.out.persistence.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "sharing_settings")
@CompoundIndex(def = "{'fromUserId': 1, 'toUserId': 1}", unique = true)
public class SharingSettingsEntity {

    @Id
    private String id;

    private String fromUserId;
    private String toUserId;
    private boolean shareLocation;
    private boolean sharePlaces;
    private boolean shareVisits;
    private Instant updatedAt;
}
