package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.AmigosConverter;
import analu.whereio.adapters.in.web.dto.response.AmigoPerfilResponse;
import analu.whereio.application.ports.in.amigos.ListarAmigosUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalAmigosController {

    private final ListarAmigosUsecase listarAmigosUsecase;
    private final AmigosConverter converter;

    @GetMapping("/amigos/{userId}")
    ResponseEntity<List<AmigoPerfilResponse>> listarAmigos(@PathVariable String userId) {
        List<AmigoPerfilResponse> lista = listarAmigosUsecase.execute(userId).stream()
                .map(converter::toAmigoResponse)
                .toList();
        return ResponseEntity.ok(lista);
    }
}
