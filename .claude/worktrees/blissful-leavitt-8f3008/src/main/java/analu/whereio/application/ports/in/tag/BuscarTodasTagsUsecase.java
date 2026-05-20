package analu.whereio.application.ports.in.tag;
import analu.whereio.application.model.Tag;
import java.util.List;
public interface BuscarTodasTagsUsecase {
    List<Tag> execute(String userId);
}
