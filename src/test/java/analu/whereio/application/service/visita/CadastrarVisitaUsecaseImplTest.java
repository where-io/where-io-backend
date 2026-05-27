package analu.whereio.application.service.visita;

import analu.whereio.application.model.Local;
import analu.whereio.application.model.Visita;
import analu.whereio.application.ports.out.LocalRepositoryPort;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CadastrarVisitaUsecaseImpl")
class CadastrarVisitaUsecaseImplTest {

    private static final String USER_ID = "user-123";

    @Mock
    private LocalRepositoryPort localRepositoryPort;

    @Mock
    private VisitaRepositoryPort visitaRepositoryPort;

    @InjectMocks
    private CadastrarVisitaUsecaseImpl cadastrarVisitaUsecase;

    private Visita visita;

    @BeforeEach
    void setUp() {
        visita = new Visita();
        visita.setIdLocal("local-123");
        visita.setUserId(USER_ID);
        visita.setDataVisita(LocalDate.of(2026, 4, 5));
        visita.setAvaliacao(4);
        visita.setComentario("Ótimo lugar");
    }

    @Nested
    @DisplayName("Quando userId da visita é inválido")
    class QuandoUserIdInvalido {

        @Test
        @DisplayName("deve lançar BAD_REQUEST quando userId está em branco")
        void deveLancarQuandoUserIdEmBranco() {
            visita.setUserId(" ");

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> cadastrarVisitaUsecase.execute(visita)
            );

            assertAll(
                    () -> assertEquals("Usuário da visita é obrigatório", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatus())
            );
            verify(localRepositoryPort, never()).buscarPorIdLocal(any());
            verify(visitaRepositoryPort, never()).adicionarVisita(any());
        }
    }

    @Nested
    @DisplayName("Quando o local não existe ou não pertence ao usuário")
    class QuandoLocalNaoExiste {

        @Test
        @DisplayName("deve lançar BusinessException com status NOT_FOUND quando buscarPorIdLocal retorna null")
        void deveLancarBusinessExceptionQuandoLocalNaoEncontrado() {
            when(localRepositoryPort.buscarPorIdLocal("local-123")).thenReturn(null);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> cadastrarVisitaUsecase.execute(visita)
            );

            assertAll(
                    () -> assertEquals("Não existe restaurante com esse id", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus())
            );
            verify(visitaRepositoryPort, never()).adicionarVisita(any());
        }

        @Test
        @DisplayName("deve lançar NOT_FOUND quando o local pertence a outro usuário")
        void deveLancarQuandoOwnerDiferente() {
            Local local = new Local();
            local.setId("local-123");
            local.setOwnerUserId("outro-user");
            when(localRepositoryPort.buscarPorIdLocal("local-123")).thenReturn(local);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> cadastrarVisitaUsecase.execute(visita)
            );

            assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus());
            verify(visitaRepositoryPort, never()).adicionarVisita(any());
        }
    }

    @Nested
    @DisplayName("Quando o local não permite visitação")
    class QuandoVisitacaoDesabilitada {

        @Test
        @DisplayName("deve lançar UNPROCESSABLE_ENTITY quando visitacao=false")
        void deveLancarQuandoVisitacaoFalse() {
            Local local = new Local();
            local.setId("local-123");
            local.setOwnerUserId(USER_ID);
            local.setVisitacao(false);
            when(localRepositoryPort.buscarPorIdLocal("local-123")).thenReturn(local);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> cadastrarVisitaUsecase.execute(visita)
            );

            assertAll(
                    () -> assertEquals("Este local não permite registro de visitas", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, excecao.getStatus())
            );
            verify(visitaRepositoryPort, never()).adicionarVisita(any());
        }
    }

    @Nested
    @DisplayName("Quando o local existe")
    class QuandoLocalExiste {

        @Test
        @DisplayName("deve retornar o id gerado quando visita é cadastrada com sucesso")
        void deveRetornarIdQuandoCadastroComSucesso() {
            Local local = new Local();
            local.setId("local-123");
            local.setOwnerUserId(USER_ID);
            when(localRepositoryPort.buscarPorIdLocal("local-123")).thenReturn(local);
            when(visitaRepositoryPort.adicionarVisita(visita)).thenReturn("visita-456");

            String idGerado = cadastrarVisitaUsecase.execute(visita);

            assertAll(
                    () -> assertEquals("visita-456", idGerado),
                    () -> verify(visitaRepositoryPort).adicionarVisita(visita)
            );
        }

        @Test
        @DisplayName("deve lançar BusinessException com status INTERNAL_SERVER_ERROR quando adicionarVisita lança Exception")
        void deveLancarBusinessExceptionQuandoAdicionarVisitaFalha() {
            Local local = new Local();
            local.setId("local-123");
            local.setOwnerUserId(USER_ID);
            when(localRepositoryPort.buscarPorIdLocal("local-123")).thenReturn(local);
            when(visitaRepositoryPort.adicionarVisita(visita)).thenThrow(new RuntimeException("erro de persistência"));

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> cadastrarVisitaUsecase.execute(visita)
            );

            assertAll(
                    () -> assertEquals("Ocorreu um erro ao cadastrar a visita", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, excecao.getStatus())
            );
        }
    }
}
