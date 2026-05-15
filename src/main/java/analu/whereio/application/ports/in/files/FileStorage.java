package analu.whereio.application.ports.in.files;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    String saveFile(MultipartFile file) throws Exception;

}
