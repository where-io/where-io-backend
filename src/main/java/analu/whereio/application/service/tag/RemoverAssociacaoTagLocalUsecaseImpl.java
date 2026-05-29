package analu.whereio.application.service.tag;

import analu.whereio.application.model.Local;
import analu.whereio.application.ports.in.tag.RemoverAssociacaoTagLocalUsecase;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.application.ports.out.TagRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RemoverAssociacaoTagLocalUsecaseImpl implements RemoverAssociacaoTagLocalUsecase {

    private final LocalRepositoryPort localRepositoryPort;
    private final TagRepositoryPort tagRepositoryPort;

    @Override
    public void execute(String idLocal, String idTag, String userId) {
        Local local = localRepositoryPort.buscarPorIdLocal(idLocal);
        if (local == null || local.getOwnerUserId() == null || !local.getOwnerUserId().equals(userId)) {
            throw new BusinessException("Local não encontrado", HttpStatus.NOT_FOUND);
        }
        List<String> idTags = local.getIdTags();
        if (idTags == null || !idTags.contains(idTag)) {
            throw new BusinessException("Associação entre local e tag não encontrada", HttpStatus.NOT_FOUND);
        }
        try {
            idTags.remove(idTag);
            localRepositoryPort.atualizarLocal(local);
            if (!localRepositoryPort.existsLocalComTag(idTag, local.getOwnerUserId())) {
                tagRepositoryPort.removerTagPorId(idTag);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Ocorreu um erro ao remover a associação", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
