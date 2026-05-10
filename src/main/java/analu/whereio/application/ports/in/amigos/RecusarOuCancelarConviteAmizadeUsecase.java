package analu.whereio.application.ports.in.amigos;

public interface RecusarOuCancelarConviteAmizadeUsecase {

    void execute(String friendshipId, String currentUserId);
}
