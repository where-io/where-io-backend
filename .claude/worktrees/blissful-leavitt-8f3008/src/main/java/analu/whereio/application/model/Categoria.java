package analu.whereio.application.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {
    /** Preenchido nas respostas da API quando a tag já está persistida. */
    private String id;
    private String nome;
    private String cor;
}

