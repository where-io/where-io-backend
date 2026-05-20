package analu.whereio.application.ports.in.tag;
import analu.whereio.application.model.Tag;
import java.util.List;
public interface BuscarTagsPorLocalUsecase {
    List<Tag> execute(String idLocal, String userId);
}
