package analu.whereio.application.service.usuario;

import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarNomeUsuarioUsecaseImpl")
class AtualizarNomeUsuarioUsecaseImplTest {

    @Mock
    private UserAccountRepositoryPort userAccountRepositoryPort;

    @InjectMocks
    private AtualizarNomeUsuarioUsecaseImpl usecase;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("deve atualizar nome e persistir")
        void deveAtualizarNomeEPersistir() {
            UserAccount conta = conta("user-1");
            when(userAccountRepositoryPort.findById("user-1")).thenReturn(Optional.of(conta));
            when(userAccountRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserAccount resultado = usecase.execute("user-1", "Novo Nome");

            ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
            verify(userAccountRepositoryPort).save(captor.capture());
            assertEquals("Novo Nome", captor.getValue().getNome());
            assertEquals("Novo Nome", resultado.getNome());
        }

        @Test
        @DisplayName("deve lançar NOT_FOUND quando usuário não existe")
        void deveLancarNotFoundQuandoNaoExiste() {
            when(userAccountRepositoryPort.findById("x")).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> usecase.execute("x", "Nome"));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
            verify(userAccountRepositoryPort, never()).save(any());
        }
    }

    private static UserAccount conta(String id) {
        UserAccount u = new UserAccount();
        u.setId(id);
        u.setNomeUsuario("user123");
        u.setNome("Nome Antigo");
        u.setEmail(id + "@test.local");
        return u;
    }
}
