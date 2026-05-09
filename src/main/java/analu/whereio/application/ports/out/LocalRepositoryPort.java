package analu.whereio.application.ports.out;

import analu.whereio.application.model.Local;

import java.util.List;

public interface LocalRepositoryPort {

        Local cadastrarLocal(Local local);

        Local buscarPorNomeLocal(String nome);

        List<Local> buscarTodosLocal();

        Local buscarPorIdLocal(String id);

        void atualizarLocal(Local local);

        void removerLocalPorId(String id);

        Local buscarPorCep(String cep);
}
