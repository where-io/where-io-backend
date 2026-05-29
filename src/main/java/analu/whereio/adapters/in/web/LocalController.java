package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.LocalConverter;
import analu.whereio.adapters.in.web.dto.request.local.LocalBuscarDtoRequest;
import analu.whereio.adapters.in.web.dto.request.local.LocalDtoRequest;
import analu.whereio.adapters.in.web.dto.request.local.LocalUpdateDtoRequest;
import analu.whereio.adapters.in.web.dto.response.LocalBuscarDtoResponse;
import analu.whereio.adapters.in.web.dto.response.LocalDtoResponse;
import analu.whereio.adapters.in.web.dto.response.PlaceDetailsDtoResponse;
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
    private final BuscarDetalhesPlaceUsecase buscarDetalhesPlaceUsecase;
    private final BuscarTodosLocalUsecase buscarTodosLocalUsecase;
    private final RemoverLocalUsecase removerLocalUsecase;

    private final LocalConverter mapper;

    @PostMapping
    ResponseEntity<String> cadastrarLocal(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody LocalDtoRequest localDtoRequest) {
        log.info("Iniciando cadastro de local. userId={} nome={} endereco={}", principal.getUserId(), localDtoRequest.getNome(), localDtoRequest.getEndereco());
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
        log.info("Iniciando remocao de local. userId={} id={}", principal.getUserId(), id);
        removerLocalUsecase.execute(id, principal.getUserId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/buscar-local")
    ResponseEntity<LocalBuscarDtoResponse> buscarLocal(@Valid @RequestBody LocalBuscarDtoRequest localBuscarDtoRequest) {
        log.info("Iniciando busca de local. inputText={}", localBuscarDtoRequest.getInputText());
        LocalBuscarDtoResponse localBuscarDtoResponse = mapper.toBuscarResponse(buscarLocalUsecase.execute(localBuscarDtoRequest.getInputText(), localBuscarDtoRequest.getSessionToken()));
        return ResponseEntity.status(HttpStatus.OK).body(localBuscarDtoResponse);
    }

    @GetMapping("/place-details/{placeId}")
    ResponseEntity<PlaceDetailsDtoResponse> detalhesPlace(@PathVariable String placeId) {
        log.info("Iniciando busca de detalhes do place. placeId={}", placeId);
        PlaceDetailsDtoResponse body = mapper.toPlaceDetailsResponse(buscarDetalhesPlaceUsecase.execute(placeId));
        return ResponseEntity.ok(body);
    }

    @GetMapping("/all")
    ResponseEntity<List<LocalDtoResponse>> buscarTodosLocais(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Iniciando listagem de locais. userId={} page={} size={}", principal.getUserId(), page, size);
        List<LocalDtoResponse> listaLocalDtoResponse = buscarTodosLocalUsecase
                .execute(principal.getUserId(), page, size)
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(listaLocalDtoResponse);
    }

    @PutMapping("/{id}")
    ResponseEntity<Void> atualizarLocal(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody LocalUpdateDtoRequest localUpdateDtoRequest) {
        log.info("Iniciando atualizacao de local. userId={} id={} request={}", principal.getUserId(), id, localUpdateDtoRequest.toString());
        atualizarLocalUsecase.execute(mapper.toDomain(localUpdateDtoRequest), id, principal.getUserId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
