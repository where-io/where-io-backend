package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.AmigosConverter;
import analu.whereio.adapters.in.web.dto.request.EnviarConviteAmizadeRequest;
import analu.whereio.adapters.in.web.dto.response.AmigoPerfilResponse;
import analu.whereio.adapters.in.web.dto.response.AmizadeConviteResponse;
import analu.whereio.adapters.in.web.dto.response.ConviteEnviadoResponse;
import analu.whereio.adapters.in.web.dto.response.EnviarConviteAmizadeResponse;
import analu.whereio.application.model.Friendship;
import analu.whereio.application.ports.in.amigos.AceitarConviteAmizadeUsecase;
import analu.whereio.application.ports.in.amigos.EnviarConviteAmizadeUsecase;
import analu.whereio.application.ports.in.amigos.ListarAmigosUsecase;
import analu.whereio.application.ports.in.amigos.ListarConvitesEnviadosAmizadeUsecase;
import analu.whereio.application.ports.in.amigos.ListarConvitesRecebidosAmizadeUsecase;
import analu.whereio.application.ports.in.amigos.RecusarOuCancelarConviteAmizadeUsecase;
import analu.whereio.config.security.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amigos")
@RequiredArgsConstructor
public class AmigosController {

    private static final Logger log = LoggerFactory.getLogger(AmigosController.class);

    private final EnviarConviteAmizadeUsecase enviarConviteAmizadeUsecase;
    private final AceitarConviteAmizadeUsecase aceitarConviteAmizadeUsecase;
    private final ListarConvitesRecebidosAmizadeUsecase listarConvitesRecebidosAmizadeUsecase;
    private final ListarConvitesEnviadosAmizadeUsecase listarConvitesEnviadosAmizadeUsecase;
    private final ListarAmigosUsecase listarAmigosUsecase;
    private final RecusarOuCancelarConviteAmizadeUsecase recusarOuCancelarConviteAmizadeUsecase;
    private final AmigosConverter converter;

    @PostMapping("/convites")
    ResponseEntity<EnviarConviteAmizadeResponse> enviarConvite(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody EnviarConviteAmizadeRequest body) {
        Friendship criado = enviarConviteAmizadeUsecase.execute(
                principal.getUserId(), body.getNomeUsuarioDestino());
        EnviarConviteAmizadeResponse resposta = new EnviarConviteAmizadeResponse();
        resposta.setSucesso(true);
        resposta.setMensagem("Convite enviado com sucesso.");
        resposta.setConvite(converter.toConviteResponse(criado));
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PostMapping("/convites/{id}/aceitar")
    ResponseEntity<AmizadeConviteResponse> aceitarConvite(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String id) {
        Friendship atualizado = aceitarConviteAmizadeUsecase.execute(id, principal.getUserId());
        return ResponseEntity.ok(converter.toConviteResponse(atualizado));
    }

    @DeleteMapping("/convites/{id}")
    ResponseEntity<Void> recusarOuCancelarConvite(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String id) {
        recusarOuCancelarConviteAmizadeUsecase.execute(id, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/convites/recebidos")
    ResponseEntity<List<ConviteEnviadoResponse>> listarRecebidos(@AuthenticationPrincipal JwtUserPrincipal principal) {
        List<ConviteEnviadoResponse> lista = listarConvitesRecebidosAmizadeUsecase.execute(principal.getUserId()).stream()
                .map(converter::toConviteEnviadoResponse)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/convites/enviados")
    ResponseEntity<List<ConviteEnviadoResponse>> listarEnviados(@AuthenticationPrincipal JwtUserPrincipal principal) {
        List<ConviteEnviadoResponse> lista = listarConvitesEnviadosAmizadeUsecase.execute(principal.getUserId()).stream()
                .map(converter::toConviteEnviadoResponse)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping
    ResponseEntity<List<AmigoPerfilResponse>> listarAmigos(@AuthenticationPrincipal JwtUserPrincipal principal) {
        List<AmigoPerfilResponse> lista = listarAmigosUsecase.execute(principal.getUserId()).stream()
                .map(converter::toAmigoResponse)
                .toList();
        return ResponseEntity.ok(lista);
    }
}
