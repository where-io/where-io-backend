package analu.whereio.application.service.tag;

import analu.whereio.application.model.Local;
import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.application.ports.out.TagRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuscarTagsPorLocalUsecaseImpl")
class BuscarTagsPorLocalUsecaseImplTest {

    private static final String USER_ID = "user-1";

    @Mock
    private LocalRepositoryPort localRepositoryPort;

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    @InjectMocks
    private BuscarTagsPorLocalUsecaseImpl buscarTagsPorLocalUsecaseImpl;

    private Local local;
    private Tag primeiraTag;
    private Tag segundaTag;
    private static final String ID_LOCAL = "local-id-1";

    @BeforeEach
    void setUp() {
        local = new Local();
        local.setId(ID_LOCAL);
        local.setOwnerUserId(USER_ID);
        local.setIdTags(List.of("tag-id-1", "tag-id-2"));

        primeiraTag = new Tag();
        primeiraTag.setId("tag-id-1");
        primeiraTag.setNome("Italiano");

        segundaTag = new Tag();
        segundaTag.setId("tag-id-2");
        segundaTag.setNome("Japonês");
    }

    @Nested
    @DisplayName("Quando o local possui associações com tags")
    class QuandoLocalPossuiTags {

        @Test
        @DisplayName("deve retornar a lista de tags mapeando cada idTag das associações do local")
        void deveRetornarListaDeTagsDoLocal() {
            when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(local);
            when(tagRepositoryPort.buscarPorIdTagDoUsuario("tag-id-1", USER_ID)).thenReturn(primeiraTag);
            when(tagRepositoryPort.buscarPorIdTagDoUsuario("tag-id-2", USER_ID)).thenReturn(segundaTag);

            List<Tag> resultado = buscarTagsPorLocalUsecaseImpl.execute(ID_LOCAL, USER_ID);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(2, resultado.size()),
                    () -> assertEquals("tag-id-1", resultado.get(0).getId()),
                    () -> assertEquals("tag-id-2", resultado.get(1).getId())
            );
            verify(localRepositoryPort).buscarPorIdLocal(ID_LOCAL);
            verify(tagRepositoryPort).buscarPorIdTagDoUsuario("tag-id-1", USER_ID);
            verify(tagRepositoryPort).buscarPorIdTagDoUsuario("tag-id-2", USER_ID);
        }

        @Test
        @DisplayName("deve filtrar tags nulas quando buscarPorIdTagDoUsuario retorna null para algum idTag")
        void deveIgnorarAssociacoesComTagInexistente() {
            when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(local);
            when(tagRepositoryPort.buscarPorIdTagDoUsuario("tag-id-1", USER_ID)).thenReturn(primeiraTag);
            when(tagRepositoryPort.buscarPorIdTagDoUsuario("tag-id-2", USER_ID)).thenReturn(null);

            List<Tag> resultado = buscarTagsPorLocalUsecaseImpl.execute(ID_LOCAL, USER_ID);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(1, resultado.size()),
                    () -> assertEquals("tag-id-1", resultado.get(0).getId())
            );
            verify(tagRepositoryPort).buscarPorIdTagDoUsuario("tag-id-1", USER_ID);
            verify(tagRepositoryPort).buscarPorIdTagDoUsuario("tag-id-2", USER_ID);
        }
    }

    @Nested
    @DisplayName("Quando o local não possui associações com tags")
    class QuandoLocalNaoPossuiTags {

        @Test
        @DisplayName("deve retornar lista vazia quando o local não possui nenhuma tag associada")
        void deveRetornarListaVaziaQuandoLocalNaoPossuiTags() {
            Local localSemTags = new Local();
            localSemTags.setId(ID_LOCAL);
            localSemTags.setOwnerUserId(USER_ID);
            localSemTags.setIdTags(List.of());
            when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(localSemTags);

            List<Tag> resultado = buscarTagsPorLocalUsecaseImpl.execute(ID_LOCAL, USER_ID);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertTrue(resultado.isEmpty())
            );
            verify(localRepositoryPort).buscarPorIdLocal(ID_LOCAL);
            verifyNoInteractions(tagRepositoryPort);
        }
    }
}
