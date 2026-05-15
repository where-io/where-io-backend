package analu.whereio.adapters.out.persistence.repository;

import analu.whereio.adapters.out.persistence.entity.VisitaEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitaRepository extends MongoRepository<VisitaEntity, String> {

    List<VisitaEntity> findByIdLocalAndUserId(String idLocal, String userId);
}
