package analu.whereio.application.service.tag;
import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.in.tag.BuscarTodasTagsUsecase;
import analu.whereio.application.ports.out.TagRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory; import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
@RequiredArgsConstructor
public class BuscarTodasTagsUsecaseImpl implements BuscarTodasTagsUsecase {
    private static final Logger log = LoggerFactory.getLogger(BuscarTodasTagsUsecaseImpl.class);
    private final TagRepositoryPort tagRepositoryPort;
    @Override
    public List<Tag> execute(String userId) {
        MDC.put("operation", "buscarTodasTags");
        try {
            log.info("Iniciando busca de todas as tags do usuário.");
            List<Tag> tags = tagRepositoryPort.buscarTodasTags(userId);
            log.info("Busca de todas as tags concluída. quantidade={}", tags.size());
            return tags;
        } finally {
            MDC.remove("operation");
        }
    }
}
