package analu.whereio.application.service.visita;

import analu.whereio.application.model.Visita;
import analu.whereio.application.ports.out.VisitaRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarVisitaUsecaseImpl")
class AtualizarVisitaUsecaseImplTest {

    private static final String USER_ID = "user-99";

    @Mock
    private VisitaRepositoryPort visitaRepositoryPort;

    @InjectMocks
    private AtualizarVisitaUsecaseImpl atualizarVisitaUsecase;

    private Visita visita;
    private static final String ID_VISITA = "visita-789";

    @BeforeEach
    void setUp() {
        visita = new Visita();
        visita.setIdLocal("local-123");
        visita.setDataVisita(LocalDate.of(2026, 4, 5));
        visita.setAvaliacao(3);
        visita.setComentario("Comentário atualizado");
    }

    @Nested
    @DisplayName("Quando a visita não existe")
    class QuandoVisitaNaoExiste {

        @Test
        @DisplayName("deve lançar BusinessException com status NOT_FOUND quando buscarPorId retorna null")
        void deveLancarBusinessExceptionQuandoVisitaNaoEncontrada() {
            when(visitaRepositoryPort.buscarPorId(ID_VISITA)).thenReturn(null);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> atualizarVisitaUsecase.execute(ID_VISITA, visita, USER_ID)
            );

            assertAll(
                    () -> assertEquals("Visita não encontrada", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus())
            );
            verify(visitaRepositoryPort, never()).atualizarVisita(any());
        }

        @Test
        @DisplayName("deve lançar NOT_FOUND quando userId não coincide")
        void deveLancarQuandoUsuarioDiferente() {
            Visita visitaExistente = new Visita();
            visitaExistente.setId(ID_VISITA);
            visitaExistente.setUserId("outro");
            when(visitaRepositoryPort.buscarPorId(ID_VISITA)).thenReturn(visitaExistente);

            assertThrows(
                    BusinessException.class,
                    () -> atualizarVisitaUsecase.execute(ID_VISITA, visita, USER_ID)
            );
            verify(visitaRepositoryPort, never()).atualizarVisita(any());
        }
    }

    @Nested
    @DisplayName("Quando a visita existe")
    class QuandoVisitaExiste {

        @Test
        @DisplayName("deve setar o id na visita e chamar atualizarVisita quando atualização ocorre com sucesso")
        void deveSetarIdEChamarAtualizarVisitaQuandoSucesso() {
            Visita visitaExistente = new Visita();
            visitaExistente.setId(ID_VISITA);
            visitaExistente.setUserId(USER_ID);
            when(visitaRepositoryPort.buscarPorId(ID_VISITA)).thenReturn(visitaExistente);

            atualizarVisitaUsecase.execute(ID_VISITA, visita, USER_ID);

            assertAll(
                    () -> assertEquals(ID_VISITA, visita.getId()),
                    () -> assertEquals(USER_ID, visita.getUserId()),
                    () -> verify(visitaRepositoryPort).atualizarVisita(visita)
            );
        }

        @Test
        @DisplayName("deve lançar BusinessException com status INTERNAL_SERVER_ERROR quando atualizarVisita lança RuntimeException")
        void deveLancarBusinessExceptionQuandoAtualizarVisitaFalha() {
            Visita visitaExistente = new Visita();
            visitaExistente.setId(ID_VISITA);
            visitaExistente.setUserId(USER_ID);
            when(visitaRepositoryPort.buscarPorId(ID_VISITA)).thenReturn(visitaExistente);
            doThrow(new RuntimeException("erro de persistência")).when(visitaRepositoryPort).atualizarVisita(any());

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> atualizarVisitaUsecase.execute(ID_VISITA, visita, USER_ID)
            );

            assertAll(
                    () -> assertEquals("Ocorreu um erro ao atualizar a visita", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, excecao.getStatus())
            );
        }
    }
}
