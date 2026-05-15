package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.LocalConverter;
import analu.whereio.adapters.in.web.dto.request.LocalBuscarDtoRequest;
import analu.whereio.adapters.in.web.dto.request.LocalDtoRequest;
import analu.whereio.adapters.in.web.dto.response.LocalBuscarDtoResponse;
import analu.whereio.adapters.in.web.dto.response.LocalDtoResponse;
import analu.whereio.application.model.Local;
import analu.whereio.application.ports.in.local.*;
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
@RequiredArgsConstructor
@RequestMapping("/api/local")
public class LocalController {

    private static final Logger log = LoggerFactory.getLogger(LocalController.class);

    private final CadastrarLocalUsecase cadastrarLocalUsecase;
    private final AtualizarLocalUsecase atualizarLocalUsecase;
    private final BuscarLocalUsecase buscarLocalUsecase;
    private final BuscarTodosLocalUsecase buscarTodosLocalUsecase;
    private final RemoverLocalUsecase removerLocalUsecase;

    private final LocalConverter mapper;

    @PostMapping
    ResponseEntity<String> cadastrarLocal(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody LocalDtoRequest localDtoRequest) {
        Local local = mapper.toDomain(localDtoRequest);
        local.setOwnerUserId(principal.getUserId());
        LocalDtoResponse localDtoResponse = mapper.toResponse(cadastrarLocalUsecase.execute(local));
        log.info("Local cadastrado com sucesso. id={}", localDtoResponse.getId());
        return ResponseEntity.status(HttpStatus.OK).body(localDtoResponse.getId());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deletarLocal(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String id) {
        removerLocalUsecase.execute(id, principal.getUserId());
        log.info("Local deletado com sucesso. id={}", id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/buscar-local")
    ResponseEntity<LocalBuscarDtoResponse> buscarLocal(@Valid @RequestBody LocalBuscarDtoRequest localBuscarDtoRequest) {
        LocalBuscarDtoResponse localBuscarDtoResponse = mapper.toBuscarResponse(buscarLocalUsecase.execute(localBuscarDtoRequest.getInputText(), localBuscarDtoRequest.getSessionToken()));
        log.info("Busca de local concluida.");
        return ResponseEntity.status(HttpStatus.OK).body(localBuscarDtoResponse);
    }

    @GetMapping("/all")
    ResponseEntity<List<LocalDtoResponse>> buscarTodosLocais(@AuthenticationPrincipal JwtUserPrincipal principal) {
        log.info("GET /api/local/all - buscarTodosLocais.");
        List<LocalDtoResponse> listaLocalDtoResponse = buscarTodosLocalUsecase
                .execute(principal.getUserId())
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(listaLocalDtoResponse);
    }

    @PutMapping("/{id}")
    ResponseEntity<LocalDtoResponse> atualizarLocal(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody LocalDtoRequest localDtoRequest) {
        atualizarLocalUsecase.execute(mapper.toDomain(localDtoRequest), id, principal.getUserId());
        log.info("Local atualizado com sucesso. id={}", id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
