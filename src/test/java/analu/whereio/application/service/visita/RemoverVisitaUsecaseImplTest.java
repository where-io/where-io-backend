package analu.whereio.application.service.visita;

import analu.whereio.application.ports.out.VisitaRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RemoverVisitaUsecaseImpl")
class RemoverVisitaUsecaseImplTest {

    @Mock
    private VisitaRepositoryPort visitaRepositoryPort;

    @InjectMocks
    private RemoverVisitaUsecaseImpl removerVisitaUsecase;

    private static final String ID_VISITA = "visita-001";

    @Nested
    @DisplayName("Quando a remoção ocorre com sucesso")
    class QuandoRemocaoComSucesso {

        @Test
        @DisplayName("deve chamar removerVisita com o id correto quando execução ocorre sem erros")
        void deveChamarRemoverVisitaComIdCorreto() {
            doNothing().when(visitaRepositoryPort).removerVisita(ID_VISITA);

            removerVisitaUsecase.execute(ID_VISITA);

            verify(visitaRepositoryPort).removerVisita(ID_VISITA);
        }
    }

    @Nested
    @DisplayName("Quando a remoção falha")
    class QuandoRemocaoFalha {

        @Test
        @DisplayName("deve lançar BusinessException com status INTERNAL_SERVER_ERROR quando removerVisita lança RuntimeException")
        void deveLancarBusinessExceptionQuandoRemoverVisitaFalha() {
            doThrow(new RuntimeException("erro de banco")).when(visitaRepositoryPort).removerVisita(ID_VISITA);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> removerVisitaUsecase.execute(ID_VISITA)
            );

            assertAll(
                    () -> assertEquals("Erro ao remover visita: ", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, excecao.getStatus())
            );
        }
    }
}
