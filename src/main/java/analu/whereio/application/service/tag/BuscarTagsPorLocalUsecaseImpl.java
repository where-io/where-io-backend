package analu.whereio.application.service.tag;

import analu.whereio.application.model.Local;
import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.in.tag.BuscarTagsPorLocalUsecase;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.application.ports.out.TagRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BuscarTagsPorLocalUsecaseImpl implements BuscarTagsPorLocalUsecase {

    private final LocalRepositoryPort localRepositoryPort;
    private final TagRepositoryPort tagRepositoryPort;

    @Override
    public List<Tag> execute(String idLocal, String userId) {
        Local local = localRepositoryPort.buscarPorIdLocal(idLocal);
        if (local == null || local.getOwnerUserId() == null || !local.getOwnerUserId().equals(userId)) {
            throw new BusinessException("Local não encontrado", HttpStatus.NOT_FOUND);
        }
        if (local.getIdTags() == null || local.getIdTags().isEmpty()) {
            return List.of();
        }
        return local.getIdTags()
                .stream()
                .map(id -> tagRepositoryPort.buscarPorIdTagDoUsuario(id, userId))
                .filter(Objects::nonNull)
                .toList();
    }
}
