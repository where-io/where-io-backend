package analu.whereio.adapters.out.persistence.impl;

import analu.whereio.adapters.out.persistence.entity.LocalEntity;
import analu.whereio.adapters.out.persistence.mapper.LocalPersistenceMapper;
import analu.whereio.adapters.out.persistence.repository.LocalRepository;
import analu.whereio.application.model.Local;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LocalRepositoryAdapter implements LocalRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(LocalRepositoryAdapter.class);

    private final LocalRepository repository;
    private final LocalPersistenceMapper mapper;


    @Override
    public Local cadastrarLocal(Local local) {
        Local salvo = mapper.toDomain(repository.save(mapper.toEntity(local)));
        log.debug("Local salvo no MongoDB. id={}", salvo.getId());
        return salvo;
    }

    @Override
    public Local buscarPorNomeLocal(String nome) {
        Local resultado = mapper.toDomain(repository.findByNome(nome));
        return resultado;
    }

    @Override
    public List<Local> buscarTodosLocal() {
        log.debug("Buscando todos os locais no MongoDB.");
        List<Local> lista = repository
                .findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
        return lista;
    }

    @Override
    public Local buscarPorIdLocal(String id) {
        Local resultado = mapper.toDomain(repository.findById(id).orElse(null));
        return resultado;
    }

    @Override
    public void atualizarLocal(Local local) {
        LocalEntity localEntity = mapper.toEntity(local);
        repository.save(localEntity);
        log.debug("Local atualizado no MongoDB. id={}", local.getId());
    }

    @Override
    public void removerLocalPorId(String id) {
        repository.deleteById(id);
        log.debug("Local removido do MongoDB. id={}", id);
    }

    @Override
    public Local buscarPorCep(String cep) {
        Local resultado = mapper.toDomain(repository.findByEndereco_Cep(cep));
        log.debug("Resultado da busca por CEP. encontrado={}", resultado != null);
        return resultado;
    }
}
