package analu.whereio.application.service.amigos;

import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.FriendshipStatus;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnviarConviteAmizadeUsecaseImpl")
class EnviarConviteAmizadeUsecaseImplTest {

    private static final String USER_A = "user-a";
    private static final String USER_B = "user-b";

    @Mock
    private FriendshipRepositoryPort friendshipRepositoryPort;

    @Mock
    private UserAccountRepositoryPort userAccountRepositoryPort;

    @InjectMocks
    private EnviarConviteAmizadeUsecaseImpl enviarConviteAmizadeUsecase;

    @Nested
    @DisplayName("Validações")
    class Validacoes {

        @Test
        @DisplayName("deve lançar BAD_REQUEST ao convidar a si mesmo")
        void naoPodeConvidarSiMesmo() {
            when(userAccountRepositoryPort.findByNome(eq("user-a")))
                    .thenReturn(Optional.of(conta(USER_A, "user-a")));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> enviarConviteAmizadeUsecase.execute(USER_A, "user-a"));

            assertAll(
                    () -> assertEquals("Não é possível convidar a si mesmo", ex.getMessage()),
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus())
            );
            verify(userAccountRepositoryPort).findByNome("user-a");
        }

        @Test
        @DisplayName("deve lançar NOT_FOUND quando o destinatário não existe")
        void destinatarioDeveExistir() {
            when(userAccountRepositoryPort.findByNome(eq("user-b"))).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> enviarConviteAmizadeUsecase.execute(USER_A, "user-b"));

            assertAll(
                    () -> assertEquals(HttpStatus.NOT_FOUND, ex.getStatus()),
                    () -> assertEquals("Nenhum usuário encontrado com este nome de usuário.", ex.getMessage())
            );
            verify(friendshipRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar CONFLICT quando já são amigos")
        void jaSaoAmigos() {
            when(userAccountRepositoryPort.findByNome(eq("user-b")))
                    .thenReturn(Optional.of(conta(USER_B, "user-b")));

            Friendship aceito = new Friendship();
            aceito.setStatus(FriendshipStatus.ACCEPTED);
            when(friendshipRepositoryPort.findAllBetweenUsers(USER_A, USER_B)).thenReturn(List.of(aceito));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> enviarConviteAmizadeUsecase.execute(USER_A, "user-b"));

            assertEquals(HttpStatus.CONFLICT, ex.getStatus());
            verify(friendshipRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar CONFLICT quando já há convite pendente")
        void convitePendente() {
            when(userAccountRepositoryPort.findByNome(eq("user-b")))
                    .thenReturn(Optional.of(conta(USER_B, "user-b")));

            Friendship pendente = new Friendship();
            pendente.setStatus(FriendshipStatus.PENDING);
            when(friendshipRepositoryPort.findAllBetweenUsers(USER_A, USER_B)).thenReturn(List.of(pendente));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> enviarConviteAmizadeUsecase.execute(USER_A, "user-b"));

            assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        }
    }

    @Nested
    @DisplayName("Fluxo feliz")
    class FluxoFeliz {

        @Test
        @DisplayName("deve persistir convite PENDING")
        void persisteConvite() {
            when(userAccountRepositoryPort.findByNome(eq("User-B")))
                    .thenReturn(Optional.of(conta(USER_B, "user-b")));
            when(friendshipRepositoryPort.findAllBetweenUsers(USER_A, USER_B)).thenReturn(List.of());
            when(friendshipRepositoryPort.save(any(Friendship.class))).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<Friendship> captor = ArgumentCaptor.forClass(Friendship.class);
            Friendship resultado = enviarConviteAmizadeUsecase.execute(USER_A, "User-B");

            verify(friendshipRepositoryPort).save(captor.capture());
            assertAll(
                    () -> assertEquals(FriendshipStatus.PENDING, captor.getValue().getStatus()),
                    () -> assertEquals(USER_A, captor.getValue().getRequesterUserId()),
                    () -> assertEquals(USER_B, captor.getValue().getAddresseeUserId()),
                    () -> assertEquals(FriendshipStatus.PENDING, resultado.getStatus())
            );
        }
    }

    private static UserAccount conta(String id, String nomeLookup) {
        UserAccount u = new UserAccount();
        u.setId(id);
        u.setNome(nomeLookup);
        u.setNomeUsuario(nomeLookup);
        u.setEmail(id + "@t.test");
        return u;
    }
}
