package analu.whereio.adapters.out.persistence.repository;

import analu.whereio.adapters.out.persistence.entity.TagEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends MongoRepository<TagEntity, String> {

    TagEntity findByNomeAndUserId(String nome, String userId);

    List<TagEntity> findAllByUserId(String userId);

    Optional<TagEntity> findByIdAndUserId(String id, String userId);

    List<TagEntity> findAllByIdInAndUserId(List<String> ids, String userId);
}
