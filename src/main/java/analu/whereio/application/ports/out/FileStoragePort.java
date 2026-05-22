package analu.whereio.application.ports.out;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
     * Local: /media/{key}  |  S3: URL pre-signed com expiração configurável.
     */
    String gerarUrlAssinada(String key);
}
