package analu.whereio.application.ports.in.local;

public interface RemoverFotoLocalUsecase {

    /** Remove o arquivo do disco e da lista de fotos do local (somente dono). */
    void execute(String idLocal, String fileName, String ownerUserId);
}
