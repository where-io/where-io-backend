package analu.whereio.application.service.tag;

import analu.whereio.application.model.Local;
import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.in.tag.AssociarTagLocalUsecase;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.application.ports.out.TagRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AssociarTagLocalUsecaseImpl implements AssociarTagLocalUsecase {

    private final LocalRepositoryPort localRepositoryPort;
    private final TagRepositoryPort tagRepositoryPort;

    @Override
    public void execute(String idLocal, String idTag, String userId) {
        Local local = localRepositoryPort.buscarPorIdLocal(idLocal);
        if (local == null || local.getOwnerUserId() == null || !local.getOwnerUserId().equals(userId)) {
            throw new BusinessException("Local não encontrado", HttpStatus.NOT_FOUND);
        }
        Tag tag = tagRepositoryPort.buscarPorIdTagDoUsuario(idTag, userId);
        if (tag == null) {
            throw new BusinessException("Tag não encontrada", HttpStatus.NOT_FOUND);
        }
        List<String> idTags = local.getIdTags();
        if (idTags == null) {
            idTags = new ArrayList<>();
            local.setIdTags(idTags);
        }
        if (idTags.contains(idTag)) {
            throw new BusinessException("Associação entre local e tag já existe", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        try {
            idTags.add(idTag);
            localRepositoryPort.atualizarLocal(local);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Ocorreu um erro ao associar a tag ao local", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
