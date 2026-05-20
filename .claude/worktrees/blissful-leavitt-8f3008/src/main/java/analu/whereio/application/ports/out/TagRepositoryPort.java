package analu.whereio.application.ports.out;

import analu.whereio.application.model.Tag;

import java.util.List;

public interface TagRepositoryPort {

    Tag cadastrarTag(Tag tag);

    Tag buscarPorIdTag(String id);

    Tag buscarPorIdTagDoUsuario(String id, String userId);

    Tag buscarPorNomeTag(String nome, String userId);

    List<Tag> buscarTodasTags(String userId);

    List<Tag> buscarPorIds(List<String> ids, String userId);

    void atualizarTag(Tag tag);

    void removerTagPorId(String id);
}
