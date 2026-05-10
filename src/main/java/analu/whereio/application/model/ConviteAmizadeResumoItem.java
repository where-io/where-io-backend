package analu.whereio.application.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Lista de convites sem expor IDs de usuários.
 * Em convites enviados, {@code nome} é o destinatário; em recebidos, é quem enviou (solicitante).
 */
@Getter
@Setter
public class ConviteAmizadeResumoItem {

    private String id;
    private String nome;
    private FriendshipStatus status;
    private Instant createdAt;
    private Instant acceptedAt;
}
