package analu.whereio.application.service.usuario;

import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ObterPerfilUsuarioUsecaseImpl")
class ObterPerfilUsuarioUsecaseImplTest {

    @Mock
    private UserAccountRepositoryPort userAccountRepositoryPort;

    @InjectMocks
    private ObterPerfilUsuarioUsecaseImpl usecase;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("deve retornar UserAccount quando usuário existe")
        void deveRetornarUserAccountQuandoExiste() {
            UserAccount conta = conta("user-1", "joao123");
            when(userAccountRepositoryPort.findById("user-1")).thenReturn(Optional.of(conta));

            UserAccount resultado = usecase.execute("user-1");

            assertEquals("user-1", resultado.getId());
            assertEquals("joao123", resultado.getNomeUsuario());
            verify(userAccountRepositoryPort).findById("user-1");
        }

        @Test
        @DisplayName("deve lançar NOT_FOUND quando usuário não existe")
        void deveLancarNotFoundQuandoNaoExiste() {
            when(userAccountRepositoryPort.findById("inexistente")).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> usecase.execute("inexistente"));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        }
    }

    private static UserAccount conta(String id, String nomeUsuario) {
        UserAccount u = new UserAccount();
        u.setId(id);
        u.setNomeUsuario(nomeUsuario);
        u.setNome("João Silva");
        u.setEmail(id + "@test.local");
        return u;
    }
}
