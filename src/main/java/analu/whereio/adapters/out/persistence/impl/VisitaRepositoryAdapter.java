package analu.whereio.adapters.out.persistence.impl;

import analu.whereio.adapters.out.persistence.mapper.VisitaPersistanceMapper;
import analu.whereio.adapters.out.persistence.repository.VisitaRepository;
import analu.whereio.application.model.Visita;
import analu.whereio.application.ports.out.VisitaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VisitaRepositoryAdapter implements VisitaRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(VisitaRepositoryAdapter.class);

    private final VisitaRepository repository;
    private final VisitaPersistanceMapper mapper;

    @Override
    public String adicionarVisita(Visita visita) {
        String id = repository.save(mapper.toEntity(visita)).getId();
        log.debug("Visita salva no MongoDB. id={}", id);
        return id;
    }

    @Override
    public void removerVisita(String id) {
        repository.deleteById(id);
        log.debug("Visita removida do MongoDB. id={}", id);
    }

    @Override
    public void atualizarVisita(Visita visita) {
        repository.save(mapper.toEntity(visita));
        log.debug("Visita atualizada no MongoDB. id={}", visita.getId());
    }

    @Override
    public List<Visita> buscarVisitasPorIdLocal(String idLocal) {
        log.debug("Buscando visitas por idLocal no MongoDB. idLocal={}", idLocal);
        List<Visita> lista = repository
                .findByIdLocal(idLocal)
                .stream()
                .map(mapper::toDomain)
                .toList();
        return lista;
    }

    @Override
    public Visita buscarPorId(String id) {
        log.debug("Buscando visita por id no MongoDB. id={}", id);
        Visita resultado = repository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);
        return resultado;
    }
}
