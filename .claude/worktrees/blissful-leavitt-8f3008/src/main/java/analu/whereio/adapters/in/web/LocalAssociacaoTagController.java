package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.TagConverter;
import analu.whereio.adapters.in.web.dto.response.TagDtoResponse;
import analu.whereio.application.ports.in.tag.AssociarTagLocalUsecase;
import analu.whereio.application.ports.in.tag.BuscarTagsPorLocalUsecase;
import analu.whereio.application.ports.in.tag.RemoverAssociacaoTagLocalUsecase;
import analu.whereio.config.security.JwtUserPrincipal;
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
@RequestMapping("/api/local/{idLocal}")
public class LocalAssociacaoTagController {
    private static final Logger log = LoggerFactory.getLogger(LocalAssociacaoTagController.class);
    private final AssociarTagLocalUsecase assocTagLocalUsecase;
    private final RemoverAssociacaoTagLocalUsecase removerAssocTagLocalUsecase;
    private final BuscarTagsPorLocalUsecase buscarTagsPorLocalUsecase;
    private final TagConverter mapper;

    @PostMapping("/tag/{idTag}")
    ResponseEntity<Void> associarTagAoLocal(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String idLocal,
            @PathVariable String idTag) {
        log.info("POST /api/local/{}/tag/{} - associarTagAoLocal.", idLocal, idTag);
        assocTagLocalUsecase.execute(idLocal, idTag, principal.getUserId());
        log.info("Tag associada ao local com sucesso. idLocal={} idTag={}", idLocal, idTag);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/tag/{idTag}")
    ResponseEntity<Void> removerAssociacaoTagDoLocal(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String idLocal,
            @PathVariable String idTag) {
        log.info("DELETE /api/local/{}/tag/{} - removerAssociacaoTagDoLocal.", idLocal, idTag);
        removerAssocTagLocalUsecase.execute(idLocal, idTag, principal.getUserId());
        log.info("Associação removida com sucesso. idLocal={} idTag={}", idLocal, idTag);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/tags")
    ResponseEntity<List<TagDtoResponse>> buscarTagsDoLocal(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String idLocal) {
        log.info("GET /api/local/{}/tags - buscarTagsDoLocal.", idLocal);
        List<TagDtoResponse> tags = buscarTagsPorLocalUsecase.execute(idLocal, principal.getUserId()).stream().map(mapper::toResponse).toList();
        log.info("Tags do local retornadas. idLocal={} quantidade={}", idLocal, tags.size());
        return ResponseEntity.status(HttpStatus.OK).body(tags);
    }
}
