package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.CompartilhamentoConverter;
import analu.whereio.adapters.in.web.converter.LocalConverter;
import analu.whereio.adapters.in.web.converter.VisitaConverter;
import analu.whereio.adapters.in.web.dto.request.AtualizarCompartilhamentoRequest;
import analu.whereio.adapters.in.web.dto.response.CompartilhamentoResponse;
import analu.whereio.adapters.in.web.dto.response.FriendSharedLocalResponse;
import analu.whereio.adapters.in.web.dto.response.LocalDtoResponse;
import analu.whereio.adapters.in.web.dto.response.VisitaDtoResponse;
import analu.whereio.application.model.SharingSettings;
import analu.whereio.application.ports.in.compartilhamento.AtualizarCompartilhamentoUsecase;
import analu.whereio.application.ports.in.compartilhamento.ObterCompartilhamentoUsecase;
import analu.whereio.application.ports.in.compartilhamento.ObterLocaisCompartilhadosUsecase;
import analu.whereio.application.ports.in.compartilhamento.ObterLocaisAmigoUsecase;
import analu.whereio.application.ports.in.compartilhamento.ObterVisitasAmigoUsecase;
import analu.whereio.config.security.JwtUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amigos")
@RequiredArgsConstructor
public class CompartilhamentoController {

    private final ObterCompartilhamentoUsecase obterCompartilhamentoUsecase;
    private final AtualizarCompartilhamentoUsecase atualizarCompartilhamentoUsecase;
    private final ObterLocaisCompartilhadosUsecase obterLocaisCompartilhadosUsecase;
    private final ObterLocaisAmigoUsecase obterLocaisAmigoUsecase;
    private final ObterVisitasAmigoUsecase obterVisitasAmigoUsecase;
    private final CompartilhamentoConverter compartilhamentoConverter;
    private final LocalConverter localConverter;
    private final VisitaConverter visitaConverter;

    @GetMapping("/{friendId}/compartilhamento")
    ResponseEntity<CompartilhamentoResponse> obterCompartilhamento(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String friendId) {
        SharingSettings settings = obterCompartilhamentoUsecase.execute(principal.getUserId(), friendId);
        return ResponseEntity.ok(compartilhamentoConverter.toResponse(settings));
    }

    @PutMapping("/{friendId}/compartilhamento")
    ResponseEntity<CompartilhamentoResponse> atualizarCompartilhamento(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String friendId,
            @RequestBody AtualizarCompartilhamentoRequest body) {
        SharingSettings updated = atualizarCompartilhamentoUsecase.execute(
                principal.getUserId(), friendId,
                body.isShareLocation(), body.isSharePlaces(), body.isShareVisits());
        return ResponseEntity.ok(compartilhamentoConverter.toResponse(updated));
    }

    @GetMapping("/{friendId}/locais")
    ResponseEntity<List<LocalDtoResponse>> obterLocaisAmigo(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String friendId) {
        List<LocalDtoResponse> locais = obterLocaisAmigoUsecase.execute(principal.getUserId(), friendId)
                .stream()
                .map(localConverter::toResponse)
                .toList();
        return ResponseEntity.ok(locais);
    }

    @GetMapping("/locais-compartilhados")
    ResponseEntity<List<FriendSharedLocalResponse>> obterLocaisCompartilhados(
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        List<FriendSharedLocalResponse> response = obterLocaisCompartilhadosUsecase.execute(principal.getUserId())
                .stream()
                .map(item -> {
                    FriendSharedLocalResponse shared = new FriendSharedLocalResponse();
                    shared.setLocal(localConverter.toResponse(item.getLocal()));
                    shared.setOwnerFriendId(item.getOwnerFriendId());
                    shared.setOwnerFriendName(item.getOwnerFriendName());
                    return shared;
                })
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{friendId}/visitas")
    ResponseEntity<List<VisitaDtoResponse>> obterVisitasAmigo(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String friendId) {
        List<VisitaDtoResponse> visitas = obterVisitasAmigoUsecase.execute(principal.getUserId(), friendId)
                .stream()
                .map(visitaConverter::toResponse)
                .toList();
        return ResponseEntity.ok(visitas);
    }
}
