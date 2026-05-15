package analu.whereio.adapters.out.persistence.repository;

import analu.whereio.adapters.out.persistence.entity.LocalEntity;
import analu.whereio.application.model.Endereco;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalRepository extends MongoRepository<LocalEntity, String> {

    LocalEntity findByNomeAndOwnerUserId(String nome, String ownerUserId);

    LocalEntity findByEndereco_CepAndOwnerUserId(String cep, String ownerUserId);

    LocalEntity findByEndereco(Endereco endereco);

    List<LocalEntity> findAllByOwnerUserId(String ownerUserId);
}
