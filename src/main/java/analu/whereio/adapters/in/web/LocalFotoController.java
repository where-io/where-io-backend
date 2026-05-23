package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.dto.response.FileUploadResponse;
import analu.whereio.application.ports.in.local.AdicionarFotoLocalUsecase;
import analu.whereio.application.ports.in.local.ListarFotosPorIdLocalUsecase;
import analu.whereio.application.ports.in.local.RemoverFotoLocalUsecase;
import analu.whereio.application.ports.out.FileStoragePort;
import analu.whereio.config.security.JwtUserPrincipal;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/local")
@RequiredArgsConstructor
public class LocalFotoController {

    private final FileStoragePort fileStoragePort;
    private final AdicionarFotoLocalUsecase adicionarFotoLocalUsecase;
    private final ListarFotosPorIdLocalUsecase listarFotosPorIdLocalUsecase;
    private final RemoverFotoLocalUsecase removerFotoLocalUsecase;

    @PostMapping(value = "/{idLocal}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> upload(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String idLocal,
            @RequestParam("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo é obrigatório", HttpStatus.BAD_REQUEST);
        }
        String storedName = adicionarFotoLocalUsecase.execute(file, idLocal, principal.getUserId());
        String urlPath = fileStoragePort.gerarUrlAssinada(storedName);
        return ResponseEntity.ok(new FileUploadResponse(storedName, urlPath));
    }

    @GetMapping("/{idLocal}/fotos")
    public ResponseEntity<List<FileUploadResponse>> listar(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String idLocal
    ) {
        List<String> nomes = listarFotosPorIdLocalUsecase.execute(idLocal, principal.getUserId());
        List<FileUploadResponse> body = nomes.stream()
                .map(n -> new FileUploadResponse(n, fileStoragePort.gerarUrlAssinada(n)))
                .toList();
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{idLocal}/fotos/{fileName:.+}")
    public ResponseEntity<List<FileUploadResponse>> remover(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String idLocal,
            @PathVariable String fileName
    ) {
        removerFotoLocalUsecase.execute(idLocal, fileName, principal.getUserId());
        List<String> remaining = listarFotosPorIdLocalUsecase.execute(idLocal, principal.getUserId());
        List<FileUploadResponse> body = remaining.stream()
                .map(n -> new FileUploadResponse(n, fileStoragePort.gerarUrlAssinada(n)))
                .toList();
        return ResponseEntity.ok(body);
    }
}
