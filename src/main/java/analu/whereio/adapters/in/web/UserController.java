package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.UserConverter;
import analu.whereio.adapters.in.web.dto.request.AtualizarNomeRequest;
import analu.whereio.adapters.in.web.dto.response.FileUploadResponse;
import analu.whereio.adapters.in.web.dto.response.UsuarioBuscaResponse;
import analu.whereio.adapters.in.web.dto.response.UsuarioPerfilResponse;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.usuario.AtualizarFotoPerfilUsecase;
import analu.whereio.application.ports.in.usuario.AtualizarNomeUsuarioUsecase;
import analu.whereio.application.ports.in.usuario.BuscarUsuariosPorPrefixoUsecase;
import analu.whereio.application.ports.in.usuario.ObterPerfilUsuarioUsecase;
import analu.whereio.application.ports.in.usuario.RemoverFotoPerfilUsecase;
import analu.whereio.application.ports.out.FileStoragePort;
import analu.whereio.config.security.JwtUserPrincipal;
import analu.whereio.exceptions.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/usuario")
@RequiredArgsConstructor
public class UserController {

    private final BuscarUsuariosPorPrefixoUsecase buscarUsuariosUsecase;
    private final ObterPerfilUsuarioUsecase obterPerfilUsecase;
    private final AtualizarNomeUsuarioUsecase atualizarNomeUsecase;
    private final AtualizarFotoPerfilUsecase atualizarFotoUsecase;
    private final RemoverFotoPerfilUsecase removerFotoUsecase;
    private final FileStoragePort fileStoragePort;
    private final UserConverter converter;

    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioBuscaResponse>> buscar(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam String q) {
        List<UsuarioBuscaResponse> resultado = buscarUsuariosUsecase.execute(q, principal.getUserId())
                .stream()
                .map(account -> {
                    UsuarioBuscaResponse resp = converter.toBuscaResponse(account);
                    if (account.getFotoPerfil() != null) {
                        resp.setFotoPerfilUrl(fileStoragePort.gerarUrlAssinada(account.getFotoPerfil()));
                    }
                    return resp;
                })
                .toList();
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioPerfilResponse> obterPerfil(
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        UserAccount account = obterPerfilUsecase.execute(principal.getUserId());
        UsuarioPerfilResponse resp = converter.toPerfilResponse(account);
        if (account.getFotoPerfil() != null) {
            resp.setFotoPerfilUrl(fileStoragePort.gerarUrlAssinada(account.getFotoPerfil()));
        }
        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/nome")
    public ResponseEntity<UsuarioPerfilResponse> atualizarNome(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody AtualizarNomeRequest request) {
        UserAccount atualizado = atualizarNomeUsecase.execute(principal.getUserId(), request.getNome());
        UsuarioPerfilResponse resp = converter.toPerfilResponse(atualizado);
        if (atualizado.getFotoPerfil() != null) {
            resp.setFotoPerfilUrl(fileStoragePort.gerarUrlAssinada(atualizado.getFotoPerfil()));
        }
        return ResponseEntity.ok(resp);
    }

    @PutMapping(value = "/foto-perfil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> atualizarFoto(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo é obrigatório", HttpStatus.BAD_REQUEST);
        }
        String key = atualizarFotoUsecase.execute(file, principal.getUserId());
        String url = fileStoragePort.gerarUrlAssinada(key);
        return ResponseEntity.ok(new FileUploadResponse(key, url));
    }

    @DeleteMapping("/foto-perfil")
    public ResponseEntity<Void> removerFoto(@AuthenticationPrincipal JwtUserPrincipal principal) {
        removerFotoUsecase.execute(principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
