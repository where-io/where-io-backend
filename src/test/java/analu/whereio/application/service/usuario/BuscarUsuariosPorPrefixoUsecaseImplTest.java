package analu.whereio.application.service.usuario;

import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.FriendshipStatus;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuscarUsuariosPorPrefixoUsecaseImpl")
class BuscarUsuariosPorPrefixoUsecaseImplTest {

    private static final String REQUESTER_ID = "requester-1";

    @Mock
    private UserAccountRepositoryPort userAccountRepositoryPort;

    @Mock
    private FriendshipRepositoryPort friendshipRepositoryPort;

    @InjectMocks
    private BuscarUsuariosPorPrefixoUsecaseImpl usecase;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("deve retornar usuários que começam com o prefixo, excluindo o próprio requester")
        void deveExcluirOProprioPerfil() {
            UserAccount self = conta(REQUESTER_ID, "joa_req");
            UserAccount other = conta("other-1", "joao123");
            when(userAccountRepositoryPort.buscarPorPrefixoNomeUsuario(eq("joa"), eq(20)))
                    .thenReturn(List.of(self, other));
            when(friendshipRepositoryPort.findAcceptedForUser(REQUESTER_ID)).thenReturn(List.of());

            List<UserAccount> resultado = usecase.execute("joa", REQUESTER_ID);

            assertEquals(1, resultado.size());
            assertEquals("joao123", resultado.get(0).getNomeUsuario());
        }

        @Test
        @DisplayName("deve excluir amigos confirmados")
        void deveExcluirAmigosConfirmados() {
            UserAccount amigo = conta("amigo-1", "joao_amigo");
            UserAccount estranho = conta("estranho-1", "joana_x");
            when(userAccountRepositoryPort.buscarPorPrefixoNomeUsuario(eq("joa"), eq(20)))
                    .thenReturn(List.of(amigo, estranho));

            Friendship amizade = new Friendship();
            amizade.setRequesterUserId(REQUESTER_ID);
            amizade.setAddresseeUserId("amigo-1");
            amizade.setStatus(FriendshipStatus.ACCEPTED);
            when(friendshipRepositoryPort.findAcceptedForUser(REQUESTER_ID)).thenReturn(List.of(amizade));

            List<UserAccount> resultado = usecase.execute("joa", REQUESTER_ID);

            assertEquals(1, resultado.size());
            assertEquals("joana_x", resultado.get(0).getNomeUsuario());
        }

        @Test
        @DisplayName("deve limitar resultados a 10")
        void deveLimitarA10() {
            List<UserAccount> candidates = new java.util.ArrayList<>();
            for (int i = 0; i < 20; i++) {
                candidates.add(conta("id-" + i, "joao" + i));
            }
            when(userAccountRepositoryPort.buscarPorPrefixoNomeUsuario(eq("joa"), eq(20)))
                    .thenReturn(candidates);
            when(friendshipRepositoryPort.findAcceptedForUser(REQUESTER_ID)).thenReturn(List.of());

            List<UserAccount> resultado = usecase.execute("joa", REQUESTER_ID);

            assertEquals(10, resultado.size());
        }

        @Test
        @DisplayName("deve normalizar prefixo para lowercase antes da query")
        void deveNormalizarPrefixoParaLowercase() {
            when(userAccountRepositoryPort.buscarPorPrefixoNomeUsuario(eq("joa"), eq(20)))
                    .thenReturn(List.of());
            when(friendshipRepositoryPort.findAcceptedForUser(REQUESTER_ID)).thenReturn(List.of());

            usecase.execute("JOA", REQUESTER_ID);

            verify(userAccountRepositoryPort).buscarPorPrefixoNomeUsuario("joa", 20);
        }
    }

    private static UserAccount conta(String id, String nomeUsuario) {
        UserAccount u = new UserAccount();
        u.setId(id);
        u.setNomeUsuario(nomeUsuario);
        u.setNome("Nome " + id);
        return u;
    }
}
