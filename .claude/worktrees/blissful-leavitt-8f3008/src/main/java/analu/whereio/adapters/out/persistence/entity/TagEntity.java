package analu.whereio.adapters.out.persistence.entity;
import lombok.Getter; import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Getter @Setter
@Document(collection = "tag_table")
public class TagEntity {
    @Id private String id;
    private String userId;
    private String nome;
    private String cor;
}
