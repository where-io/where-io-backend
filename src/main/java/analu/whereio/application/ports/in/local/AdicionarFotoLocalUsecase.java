package analu.whereio.application.ports.in.local;

import org.springframework.web.multipart.MultipartFile;

public interface AdicionarFotoLocalUsecase {

    /** Persiste o arquivo e associa o nome ao local indicado. */
    String execute(MultipartFile file, String idLocal, String ownerUserId);
}
