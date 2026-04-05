package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.VisitaConverter;
import analu.whereio.adapters.in.web.dto.request.VisitaDtoRequest;
import analu.whereio.adapters.in.web.dto.response.VisitaDtoResponse;
import analu.whereio.application.ports.in.visita.AtualizarVisitaUsecase;
import analu.whereio.application.ports.in.visita.BuscarVisitaPorIdLocalUsecase;
import analu.whereio.application.ports.in.visita.CadastrarVisitaUsecase;
import analu.whereio.application.ports.in.visita.RemoverVisitaUsecase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visita")
@RequiredArgsConstructor
public class VisitaController {

    private final CadastrarVisitaUsecase cadastrarVisitaUsecase;
    private final AtualizarVisitaUsecase atualizarVisitaUsecase;
    private final RemoverVisitaUsecase removerVisitaUsecase;
    private final BuscarVisitaPorIdLocalUsecase buscarVisitaPorIdLocalUsecase;

    private final VisitaConverter mapper;

    @GetMapping("/{idLocal}")
    public ResponseEntity<List<VisitaDtoResponse>> buscarVisitasPorIdLocal(@PathVariable String idLocal) {
        List<VisitaDtoResponse> visitas = buscarVisitaPorIdLocalUsecase.execute(idLocal);
        return ResponseEntity.status(HttpStatus.OK).body(visitas);
    }

    @PostMapping
    public ResponseEntity<String> adicionarVisita(@Valid @RequestBody VisitaDtoRequest visitaDtoRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastrarVisitaUsecase.execute(mapper.toDomain(visitaDtoRequest)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerVisita(@PathVariable String id) {
        removerVisitaUsecase.execute(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizarVisita(@PathVariable String id, @Valid @RequestBody VisitaDtoRequest visitaDtoRequest) {
        atualizarVisitaUsecase.execute(id, mapper.toDomain(visitaDtoRequest));
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
