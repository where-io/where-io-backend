package analu.whereio.application.ports.out;

import analu.whereio.application.model.Local;

import java.util.List;

public interface LocalRepositoryPort {

    Local cadastrarLocal(Local local);

    Local buscarPorNomeLocal(String nome, String ownerUserId);

    List<Local> buscarTodosLocalPorUsuario(String ownerUserId);

    List<Local> buscarTodosLocalPorUsuarioPaginado(String ownerUserId, int page, int size);

    Local buscarPorIdLocal(String id);

    void atualizarLocal(Local local);

    void removerLocalPorId(String id);

    Local buscarPorCep(String cep, String ownerUserId);

    boolean existsLocalComTag(String idTag, String userId);
}
