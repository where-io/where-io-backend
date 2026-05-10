package analu.whereio.application.service.amigos;

import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.FriendshipStatus;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AceitarConviteAmizadeUsecaseImpl")
class AceitarConviteAmizadeUsecaseImplTest {

    private static final String CONVITE_ID = "convite-1";
    private static final String SOLICITANTE = "user-a";
    private static final String DESTINATARIO = "user-b";

    @Mock
    private FriendshipRepositoryPort friendshipRepositoryPort;

    @InjectMocks
    private AceitarConviteAmizadeUsecaseImpl aceitarConviteAmizadeUsecase;

    @Nested
    @DisplayName("Validações")
    class Validacoes {

        @Test
        @DisplayName("deve lançar NOT_FOUND quando o convite não existe")
        void conviteInexistente() {
            when(friendshipRepositoryPort.findById(CONVITE_ID)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> aceitarConviteAmizadeUsecase.execute(CONVITE_ID, DESTINATARIO));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        }

        @Test
        @DisplayName("deve lançar FORBIDDEN quando quem aceita não é o destinatário")
        void somenteDestinatario() {
            Friendship pendente = pendente();
            when(friendshipRepositoryPort.findById(CONVITE_ID)).thenReturn(Optional.of(pendente));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> aceitarConviteAmizadeUsecase.execute(CONVITE_ID, SOLICITANTE));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        }

        @Test
        @DisplayName("deve lançar BAD_REQUEST quando o convite não está pendente")
        void naoPendente() {
            Friendship aceito = pendente();
            aceito.setStatus(FriendshipStatus.ACCEPTED);
            when(friendshipRepositoryPort.findById(CONVITE_ID)).thenReturn(Optional.of(aceito));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> aceitarConviteAmizadeUsecase.execute(CONVITE_ID, DESTINATARIO));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        }
    }

    @Nested
    @DisplayName("Fluxo feliz")
    class FluxoFeliz {

        @Test
        @DisplayName("deve marcar como ACCEPTED e definir acceptedAt")
        void aceitaConvite() {
            Friendship pendente = pendente();
            when(friendshipRepositoryPort.findById(CONVITE_ID)).thenReturn(Optional.of(pendente));
            when(friendshipRepositoryPort.save(any(Friendship.class))).thenAnswer(inv -> inv.getArgument(0));

            Friendship resultado = aceitarConviteAmizadeUsecase.execute(CONVITE_ID, DESTINATARIO);

            verify(friendshipRepositoryPort).save(pendente);
            assertAll(
                    () -> assertEquals(FriendshipStatus.ACCEPTED, resultado.getStatus()),
                    () -> assertNotNull(resultado.getAcceptedAt())
            );
        }
    }

    private static Friendship pendente() {
        Friendship f = new Friendship();
        f.setId(CONVITE_ID);
        f.setRequesterUserId(SOLICITANTE);
        f.setAddresseeUserId(DESTINATARIO);
        f.setStatus(FriendshipStatus.PENDING);
        return f;
    }
}
