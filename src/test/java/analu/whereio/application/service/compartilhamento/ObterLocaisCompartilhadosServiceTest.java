package analu.whereio.application.service.compartilhamento;

import analu.whereio.application.model.FriendSharedLocal;
import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.Local;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ObterLocaisCompartilhadosService")
class ObterLocaisCompartilhadosServiceTest {

    @Mock
    private FriendshipRepositoryPort friendshipRepositoryPort;
    @Mock
    private UserAccountRepositoryPort userAccountRepositoryPort;
    @Mock
    private SharingPermissaoService sharingPermissaoService;
    @Mock
    private LocalRepositoryPort localRepositoryPort;

    @InjectMocks
    private ObterLocaisCompartilhadosService service;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("deve retornar apenas locais de amigos com sharePlaces habilitado")
        void deveRetornarApenasLocaisPermitidos() {
            Friendship f1 = new Friendship();
            f1.setRequesterUserId("viewer-1");
            f1.setAddresseeUserId("friend-1");
            Friendship f2 = new Friendship();
            f2.setRequesterUserId("friend-2");
            f2.setAddresseeUserId("viewer-1");

            UserAccount u1 = new UserAccount();
            u1.setId("friend-1");
            u1.setNome("Ana");
            UserAccount u2 = new UserAccount();
            u2.setId("friend-2");
            u2.setNome("Bruno");

            Local localFriend1 = new Local();
            localFriend1.setId("local-1");
            localFriend1.setNome("Praia X");

            when(friendshipRepositoryPort.findAcceptedForUser("viewer-1")).thenReturn(List.of(f1, f2));
            when(userAccountRepositoryPort.findAllById(anyCollection())).thenReturn(List.of(u1, u2));
            when(sharingPermissaoService.podeVerLocais("viewer-1", "friend-1")).thenReturn(true);
            when(sharingPermissaoService.podeVerLocais("viewer-1", "friend-2")).thenReturn(false);
            when(localRepositoryPort.buscarTodosLocalPorUsuario("friend-1")).thenReturn(List.of(localFriend1));

            List<FriendSharedLocal> result = service.execute("viewer-1");

            assertAll(
                    () -> assertEquals(1, result.size()),
                    () -> assertEquals("local-1", result.get(0).getLocal().getId()),
                    () -> assertEquals("friend-1", result.get(0).getOwnerFriendId()),
                    () -> assertEquals("Ana", result.get(0).getOwnerFriendName())
            );

            verify(localRepositoryPort).buscarTodosLocalPorUsuario("friend-1");
        }
    }
}
