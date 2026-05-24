package analu.whereio.application.ports.in.usuario;

import org.springframework.web.multipart.MultipartFile;

public interface AtualizarFotoPerfilUsecase {
    String execute(MultipartFile file, String userId);
}
