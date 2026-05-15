package analu.whereio.application.ports.out;

import analu.whereio.application.model.Visita;

import java.util.List;

public interface VisitaRepositoryPort {

    String adicionarVisita(Visita visita);

    void removerVisita(String id);

    void atualizarVisita(Visita visita);

    List<Visita> buscarVisitasPorIdLocal(String idLocal, String userId);

    Visita buscarPorId(String id);
}
