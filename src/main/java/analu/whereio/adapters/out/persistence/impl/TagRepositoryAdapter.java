package analu.whereio.adapters.out.persistence.impl;

import analu.whereio.adapters.out.persistence.mapper.TagPersistenceMapper;
import analu.whereio.adapters.out.persistence.repository.TagRepository;
import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.out.TagRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TagRepositoryAdapter implements TagRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(TagRepositoryAdapter.class);
    private final TagRepository repository;
    private final TagPersistenceMapper mapper;

    @Override
    public Tag cadastrarTag(Tag tag) {
        log.debug("Salvando tag no MongoDB. nome={}", tag.getNome());
        return mapper.toDomain(repository.save(mapper.toEntity(tag)));
    }

    @Override
    public Tag buscarPorIdTag(String id) {
        return repository.findById(id).map(mapper::toDomain).orElse(null);
    }

    @Override
    public Tag buscarPorIdTagDoUsuario(String id, String userId) {
        return repository.findByIdAndUserId(id, userId).map(mapper::toDomain).orElse(null);
    }

    @Override
    public Tag buscarPorNomeTag(String nome, String userId) {
        var entity = repository.findByNomeAndUserId(nome, userId);
        return entity == null ? null : mapper.toDomain(entity);
    }

    @Override
    public List<Tag> buscarTodasTags(String userId) {
        return repository.findAllByUserId(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void atualizarTag(Tag tag) {
        log.debug("Atualizando tag no MongoDB. id={}", tag.getId());
        repository.save(mapper.toEntity(tag));
    }

    @Override
    public void removerTagPorId(String id) {
        repository.deleteById(id);
        log.debug("Tag removida do MongoDB. id={}", id);
    }
}
