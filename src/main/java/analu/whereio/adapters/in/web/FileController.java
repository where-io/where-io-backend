package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.dto.response.FileUploadResponse;
import analu.whereio.application.ports.in.local.AdicionarFotoLocalUsecase;
import analu.whereio.application.ports.in.local.ListarFotosPorIdLocalUsecase;
import analu.whereio.application.ports.in.local.RemoverFotoLocalUsecase;
import analu.whereio.application.service.files.FileStorageService;
import analu.whereio.config.security.JwtUserPrincipal;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping({"/api/files", "/files"})
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final AdicionarFotoLocalUsecase adicionarFotoLocalUsecase;
    private final ListarFotosPorIdLocalUsecase listarFotosPorIdLocalUsecase;
    private final RemoverFotoLocalUsecase removerFotoLocalUsecase;

    /**
     * Upload opcionalmente vinculado a um local via {@code idLocal}.
     * Arquivos públicos são servidos em {@code GET /media/{fileName}}.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> upload(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "idLocal", required = false) String idLocal
    ) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo é obrigatório", HttpStatus.BAD_REQUEST);
        }
        String storedName;
        if (idLocal != null && !idLocal.isBlank()) {
            storedName = adicionarFotoLocalUsecase.execute(file, idLocal, principal.getUserId());
        } else {
            try {
                storedName = fileStorageService.saveFile(file);
            } catch (Exception e) {
                throw new BusinessException("Erro ao salvar arquivo", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        String urlPath = "/media/" + storedName;
        return ResponseEntity.ok(new FileUploadResponse(storedName, urlPath));
    }

    /**
     * Lista todas as fotos armazenadas para o local (mesmo dono do JWT).
     */
    @GetMapping("/local/{idLocal}/fotos")
    public ResponseEntity<List<FileUploadResponse>> listarFotosDoLocal(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String idLocal
    ) {
        List<String> nomes = listarFotosPorIdLocalUsecase.execute(idLocal, principal.getUserId());
        List<FileUploadResponse> body = nomes.stream()
                .map(n -> new FileUploadResponse(n, "/media/" + n))
                .toList();
        return ResponseEntity.ok(body);
    }

    /**
     * Remove uma foto do local e apaga o arquivo do disco (somente dono).
     *
     * @param fileName nome retornado no upload (ex.: {@code uuid_original.png}), query obrigatória
     */
    @DeleteMapping("/local/{idLocal}/fotos")
    public ResponseEntity<Void> removerFotoDoLocal(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String idLocal,
            @RequestParam("fileName") String fileName
    ) {
        removerFotoLocalUsecase.execute(idLocal, fileName, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
