package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.TagConverter;
import analu.whereio.adapters.in.web.dto.request.TagDtoRequest;
import analu.whereio.adapters.in.web.dto.response.TagDtoResponse;
import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.in.tag.*;
import analu.whereio.config.security.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tag")
public class TagController {

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
        Tag tag = mapper.toDomain(tagDtoRequest);
        tag.setUserId(principal.getUserId());
        TagDtoResponse response = mapper.toResponse(cadastrarTagUsecase.execute(tag));
        return ResponseEntity.status(HttpStatus.OK).body(response.getId());
    }

    @GetMapping("/all")
    ResponseEntity<List<TagDtoResponse>> buscarTodasTags(@AuthenticationPrincipal JwtUserPrincipal principal) {
        List<TagDtoResponse> lista = buscarTodasTagsUsecase.execute(principal.getUserId()).stream().map(mapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(lista);
    }

    @GetMapping("/{id}")
    ResponseEntity<TagDtoResponse> buscarTagPorId(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String id) {
        TagDtoResponse response = mapper.toResponse(buscarTagPorIdUsecase.execute(id, principal.getUserId()));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    ResponseEntity<Void> atualizarTag(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody TagDtoRequest tagDtoRequest) {
        atualizarTagUsecase.execute(mapper.toDomain(tagDtoRequest), id, principal.getUserId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> removerTag(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String id) {
        removerTagUsecase.execute(id, principal.getUserId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
