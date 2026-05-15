package analu.whereio.application.ports.in.tag;
import analu.whereio.application.model.Tag;
public interface AtualizarTagUsecase {
    void execute(Tag tag, String id, String userId);
}
