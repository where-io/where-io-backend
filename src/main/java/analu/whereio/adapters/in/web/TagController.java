package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.TagConverter;
import analu.whereio.adapters.in.web.dto.request.TagDtoRequest;
import analu.whereio.adapters.in.web.dto.response.TagDtoResponse;
import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.in.tag.*;
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
@RequestMapping("/api/tag")
public class TagController {
    private static final Logger log = LoggerFactory.getLogger(TagController.class);

    private final CadastrarTagUsecase cadastrarTagUsecase;
    private final AtualizarTagUsecase atualizarTagUsecase;
    private final BuscarTagPorIdUsecase buscarTagPorIdUsecase;
    private final BuscarTodasTagsUsecase buscarTodasTagsUsecase;
    private final RemoverTagUsecase removerTagUsecase;
    private final TagConverter mapper;

    @PostMapping
    ResponseEntity<String> cadastrarTag(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody TagDtoRequest tagDtoRequest) {
        log.info("POST /api/tag - cadastrarTag. nome={}", tagDtoRequest.getNome());
        Tag tag = mapper.toDomain(tagDtoRequest);
        tag.setUserId(principal.getUserId());
        TagDtoResponse response = mapper.toResponse(cadastrarTagUsecase.execute(tag));
        log.info("Tag cadastrada com sucesso. id={}", response.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response.getId());
    }

    @GetMapping("/all")
    ResponseEntity<List<TagDtoResponse>> buscarTodasTags(@AuthenticationPrincipal JwtUserPrincipal principal) {
        log.info("GET /api/tag/all - buscarTodasTags.");
        List<TagDtoResponse> lista = buscarTodasTagsUsecase.execute(principal.getUserId()).stream().map(mapper::toResponse).toList();
        log.info("Busca de todas as tags concluída. quantidade={}", lista.size());
        return ResponseEntity.status(HttpStatus.OK).body(lista);
    }

    @GetMapping("/{id}")
    ResponseEntity<TagDtoResponse> buscarTagPorId(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String id) {
        log.info("GET /api/tag/{} - buscarTagPorId.", id);
        TagDtoResponse response = mapper.toResponse(buscarTagPorIdUsecase.execute(id, principal.getUserId()));
        log.info("Tag encontrada com sucesso. id={}", id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    ResponseEntity<Void> atualizarTag(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody TagDtoRequest tagDtoRequest) {
        log.info("PUT /api/tag/{} - atualizarTag.", id);
        atualizarTagUsecase.execute(mapper.toDomain(tagDtoRequest), id, principal.getUserId());
        log.info("Tag atualizada com sucesso. id={}", id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> removerTag(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String id) {
        log.info("DELETE /api/tag/{} - removerTag.", id);
        removerTagUsecase.execute(id, principal.getUserId());
        log.info("Tag removida com sucesso. id={}", id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
