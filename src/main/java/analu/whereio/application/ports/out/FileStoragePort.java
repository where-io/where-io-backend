package analu.whereio.application.ports.out;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface FileStoragePort {

    /**
     * Persiste o arquivo e retorna a key (ex: uuid_foto.jpg).
     */
    String salvar(MultipartFile file) throws IOException;

    /**
     * Remove o arquivo pelo key. Implementações devem tolerar key inexistente.
     */
    void deletar(String key) throws IOException;

    /**
     * Retorna URL de acesso ao arquivo.
     * Local: /media/{key}  |  S3: /api/files/{key} (proxy via backend).
     */
    String gerarUrlAssinada(String key);

    /**
     * Abre stream de leitura do arquivo pelo key.
     * Usado pelo endpoint proxy GET /api/files/{key}.
     */
    InputStream getObject(String key) throws IOException;
}
