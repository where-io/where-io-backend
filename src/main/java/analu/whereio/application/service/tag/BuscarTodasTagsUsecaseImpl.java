package analu.whereio.application.service.tag;

import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.in.tag.BuscarTodasTagsUsecase;
import analu.whereio.application.ports.out.TagRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarTodasTagsUsecaseImpl implements BuscarTodasTagsUsecase {

    private final TagRepositoryPort tagRepositoryPort;

    @Override
    public List<Tag> execute(String userId) {
        return tagRepositoryPort.buscarTodasTags(userId);
    }
}
