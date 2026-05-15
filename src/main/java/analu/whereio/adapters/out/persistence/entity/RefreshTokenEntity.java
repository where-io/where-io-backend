package analu.whereio.adapters.out.persistence.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "refresh_token_table")
public class RefreshTokenEntity {

    @Id
    private String id;

    private String userId;

    @Indexed(unique = true)
    private String tokenHash;

    private Instant expiresAt;
}
