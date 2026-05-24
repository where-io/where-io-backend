package analu.whereio.application.ports.out;

import analu.whereio.application.model.UserAccount;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserAccountRepositoryPort {

    UserAccount save(UserAccount user);

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findById(String id);

    List<UserAccount> findAllById(Collection<String> ids);

    Optional<UserAccount> findByNomeUsuario(String nomeUsuario);

    boolean existsByEmail(String email);

    boolean existsByNomeUsuario(String nomeUsuario);

    List<UserAccount> buscarPorPrefixoNomeUsuario(String prefix, int limit);
}
