package analu.whereio.application.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tag {
    private String id;
    /** ID do usuário que criou a tag. */
    private String userId;
    private String nome;
    private String cor;
}
