package analu.whereio.adapters.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Resposta do upload: nome no disco e caminho relativo para uso em {@code img src}
 * (combinar com a origem da API, ex.: {@code http://localhost:8080/media/...}).
 */
@Getter
@AllArgsConstructor
public class FileUploadResponse {

    private final String fileName;
    /** Caminho absoluto na aplicação, ex.: {@code /media/uuid_foto.jpg}. */
    private final String urlPath;
}
