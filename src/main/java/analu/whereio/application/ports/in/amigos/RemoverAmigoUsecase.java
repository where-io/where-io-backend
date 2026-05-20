package analu.whereio.application.ports.in.amigos;

public interface RemoverAmigoUsecase {
    void execute(String friendshipId, String currentUserId);
}
