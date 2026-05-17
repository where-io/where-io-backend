package analu.whereio.application.service.local;

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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuscarTodosLocalUsecaseImpl")
class BuscarTodosLocalUsecaseImplTest {

    @Mock
    private LocalRepositoryPort localRepositoryPort;

    @Mock
    private VisitaRepositoryPort visitaRepository;

    @Mock
    private SincronizarTagsDoLocalService sincronizarTagsDoLocalService;

    @InjectMocks
    private BuscarTodosLocalUsecaseImpl buscarTodosLocalUsecaseImpl;

    private static final String OWNER_USER_ID = "owner-1";

    private Local localUm;
    private Local localDois;
    private Visita visitaUm;
    private Visita visitaDois;

    @BeforeEach
    void setUp() {
        localUm = new Local();
        localUm.setId("id-local-1");
        localUm.setNome("Restaurante A");
        localUm.setOwnerUserId(OWNER_USER_ID);

        localDois = new Local();
        localDois.setId("id-local-2");
        localDois.setNome("Restaurante B");
        localDois.setOwnerUserId(OWNER_USER_ID);

        visitaUm = new Visita();
        visitaUm.setId("id-visita-1");
        visitaUm.setIdLocal("id-local-1");
        visitaUm.setUserId(OWNER_USER_ID);
        visitaUm.setAvaliacao(5);
        visitaUm.setComentario("Excelente!");
        visitaUm.setDataVisita(LocalDate.of(2025, 1, 10));

        visitaDois = new Visita();
        visitaDois.setId("id-visita-2");
        visitaDois.setIdLocal("id-local-2");
        visitaDois.setUserId(OWNER_USER_ID);
        visitaDois.setAvaliacao(4);
        visitaDois.setComentario("Muito bom!");
        visitaDois.setDataVisita(LocalDate.of(2025, 2, 15));

        lenient().doNothing().when(sincronizarTagsDoLocalService).hidratarParaResposta(any(Local.class));
    }

    @Nested
    @DisplayName("Quando a busca retorna locais")
    class QuandoRetornaLocais {

        @Test
        @DisplayName("deve retornar todos os locais com suas visitas populadas quando existem locais cadastrados")
        void deveRetornarLocaisComVisitasPopuladasQuandoExistemLocais() {
            when(localRepositoryPort.buscarTodosLocalPorUsuarioPaginado(OWNER_USER_ID, 0, 20)).thenReturn(List.of(localUm, localDois));
            when(visitaRepository.buscarVisitasPorIdLocal("id-local-1", OWNER_USER_ID)).thenReturn(List.of(visitaUm));
            when(visitaRepository.buscarVisitasPorIdLocal("id-local-2", OWNER_USER_ID)).thenReturn(List.of(visitaDois));

            List<Local> resultado = buscarTodosLocalUsecaseImpl.execute(OWNER_USER_ID, 0, 20);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(2, resultado.size()),
                    () -> assertEquals(1, resultado.get(0).getVisitas().size()),
                    () -> assertEquals("id-visita-1", resultado.get(0).getVisitas().get(0).getId()),
                    () -> assertEquals(1, resultado.get(1).getVisitas().size()),
                    () -> assertEquals("id-visita-2", resultado.get(1).getVisitas().get(0).getId())
            );

            verify(visitaRepository).buscarVisitasPorIdLocal("id-local-1", OWNER_USER_ID);
            verify(visitaRepository).buscarVisitasPorIdLocal("id-local-2", OWNER_USER_ID);
        }

        @Test
        @DisplayName("deve chamar buscarVisitasPorIdLocal com o id de cada local")
        void deveChamarBuscarVisitasPorIdLocalComIdCadaLocal() {
            when(localRepositoryPort.buscarTodosLocalPorUsuarioPaginado(OWNER_USER_ID, 0, 20)).thenReturn(List.of(localUm, localDois));
            when(visitaRepository.buscarVisitasPorIdLocal(anyString(), eq(OWNER_USER_ID))).thenReturn(Collections.emptyList());

            buscarTodosLocalUsecaseImpl.execute(OWNER_USER_ID, 0, 20);

            verify(visitaRepository).buscarVisitasPorIdLocal("id-local-1", OWNER_USER_ID);
            verify(visitaRepository).buscarVisitasPorIdLocal("id-local-2", OWNER_USER_ID);
            verifyNoMoreInteractions(visitaRepository);
        }
    }

    @Nested
    @DisplayName("Quando a lista de locais está vazia")
    class QuandoListaVazia {

        @Test
        @DisplayName("deve retornar lista vazia quando não existem locais cadastrados")
        void deveRetornarListaVaziaQuandoNaoExistemLocais() {
            when(localRepositoryPort.buscarTodosLocalPorUsuarioPaginado(OWNER_USER_ID, 0, 20)).thenReturn(Collections.emptyList());

            List<Local> resultado = buscarTodosLocalUsecaseImpl.execute(OWNER_USER_ID, 0, 20);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertTrue(resultado.isEmpty())
            );
            verifyNoInteractions(visitaRepository);
        }
    }

    @Nested
    @DisplayName("Quando a busca falha")
    class QuandoBuscaFalha {

        @Test
        @DisplayName("deve lançar BusinessException com INTERNAL_SERVER_ERROR quando buscarTodosLocal lança RuntimeException")
        void deveLancarBusinessExceptionQuandoBuscarTodosLocalFalha() {
            when(localRepositoryPort.buscarTodosLocalPorUsuarioPaginado(OWNER_USER_ID, 0, 20))
                    .thenThrow(new RuntimeException("Falha no banco de dados"));

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> buscarTodosLocalUsecaseImpl.execute(OWNER_USER_ID, 0, 20)
            );

            assertAll(
                    () -> assertEquals("Ocorreu um erro ao buscar os locais", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, excecao.getStatus())
            );
        }
    }
}
