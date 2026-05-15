package analu.whereio.application.service.tag;
import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.in.tag.AtualizarTagUsecase;
import analu.whereio.application.ports.out.TagRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory; import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import static java.util.Objects.isNull;
@Service
@RequiredArgsConstructor
public class AtualizarTagUsecaseImpl implements AtualizarTagUsecase {
    private static final Logger log = LoggerFactory.getLogger(AtualizarTagUsecaseImpl.class);
    private final TagRepositoryPort tagRepositoryPort;
    @Override
    public void execute(Tag tag, String id, String userId) {
        MDC.put("operation", "atualizarTag");
        MDC.put("entityId", id);
        try {
            log.info("Iniciando atualização de tag. id={}", id);
            Tag existente = tagRepositoryPort.buscarPorIdTagDoUsuario(id, userId);
            if (isNull(existente)) {
                log.warn("Tag não encontrada para atualização. id={}", id);
                throw new BusinessException("Tag não encontrada", HttpStatus.NOT_FOUND);
            }
            tag.setId(id);
            tag.setUserId(existente.getUserId());
            tagRepositoryPort.atualizarTag(tag);
            log.info("Tag atualizada com sucesso. id={}", id);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Ocorreu um erro ao atualizar a tag", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
