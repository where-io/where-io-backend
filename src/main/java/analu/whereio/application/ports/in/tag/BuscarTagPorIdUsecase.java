package analu.whereio.application.ports.in.tag;
import analu.whereio.application.model.Tag;
public interface BuscarTagPorIdUsecase {
    Tag execute(String id, String userId);
}
