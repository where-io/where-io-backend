package analu.whereio.adapters.out.persistence.impl;

import analu.whereio.adapters.out.persistence.entity.LocalEntity;
import analu.whereio.adapters.out.persistence.mapper.LocalPersistenceMapper;
import analu.whereio.adapters.out.persistence.repository.LocalRepository;
import analu.whereio.application.model.Local;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
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
    public Local buscarPorNomeLocal(String nome, String ownerUserId) {
        LocalEntity entity = repository.findByNomeAndOwnerUserId(nome, ownerUserId);
        return entity == null ? null : mapper.toDomain(entity);
    }

    @Override
    public List<Local> buscarTodosLocalPorUsuario(String ownerUserId) {
        log.debug("Buscando locais por usuário no MongoDB. ownerUserId={}", ownerUserId);
        return repository.findAllByOwnerUserId(ownerUserId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Local buscarPorIdLocal(String id) {
        LocalEntity entity = repository.findById(id).orElse(null);
        return entity == null ? null : mapper.toDomain(entity);
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
    public List<Local> buscarTodosLocalPorUsuarioPaginado(String ownerUserId, int page, int size) {
        log.debug("Buscando locais paginados por usuário no MongoDB. ownerUserId={}, page={}, size={}", ownerUserId, page, size);
        return repository.findAllByOwnerUserId(ownerUserId, PageRequest.of(page, size))
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Local buscarPorCep(String cep, String ownerUserId) {
        LocalEntity entity = repository.findByEndereco_CepAndOwnerUserId(cep, ownerUserId);
        return entity == null ? null : mapper.toDomain(entity);
    }

    @Override
    public boolean existsLocalComTag(String idTag, String userId) {
        return repository.existsByOwnerUserIdAndIdTagsContaining(userId, idTag);
    }
}
