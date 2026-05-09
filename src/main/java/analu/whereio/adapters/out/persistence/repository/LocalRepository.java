package analu.whereio.adapters.out.persistence.repository;

import analu.whereio.adapters.out.persistence.entity.LocalEntity;
import analu.whereio.application.model.Endereco;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocalRepository extends MongoRepository<LocalEntity, String> {

    LocalEntity findByNome(String nome);
    LocalEntity findByEndereco(Endereco endereco);
    LocalEntity findByEndereco_Cep(String cep);
}
