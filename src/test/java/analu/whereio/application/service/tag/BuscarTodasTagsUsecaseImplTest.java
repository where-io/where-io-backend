package analu.whereio.application.service.tag;

import analu.whereio.application.model.Tag;
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
@DisplayName("BuscarTodasTagsUsecaseImpl")
class BuscarTodasTagsUsecaseImplTest {

    private static final String USER_ID = "user-1";

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    @InjectMocks
    private BuscarTodasTagsUsecaseImpl buscarTodasTagsUsecaseImpl;

    private Tag primeiraTag;
    private Tag segundaTag;

    @BeforeEach
    void setUp() {
        primeiraTag = new Tag();
        primeiraTag.setId("tag-id-1");
        primeiraTag.setNome("Italiano");

        segundaTag = new Tag();
        segundaTag.setId("tag-id-2");
        segundaTag.setNome("Japonês");
    }

    @Nested
    @DisplayName("Quando há tags cadastradas")
    class QuandoHaTags {

        @Test
        @DisplayName("deve retornar a lista de todas as tags do repositório do usuário")
        void deveRetornarListaDeTodasAsTags() {
            when(tagRepositoryPort.buscarTodasTags(USER_ID)).thenReturn(List.of(primeiraTag, segundaTag));

            List<Tag> resultado = buscarTodasTagsUsecaseImpl.execute(USER_ID);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(2, resultado.size()),
                    () -> assertEquals("tag-id-1", resultado.get(0).getId()),
                    () -> assertEquals("tag-id-2", resultado.get(1).getId())
            );
            verify(tagRepositoryPort).buscarTodasTags(USER_ID);
        }
    }

    @Nested
    @DisplayName("Quando não há tags cadastradas")
    class QuandoNaoHaTags {

        @Test
        @DisplayName("deve retornar lista vazia quando não há tags no repositório")
        void deveRetornarListaVaziaQuandoNaoHaTags() {
            when(tagRepositoryPort.buscarTodasTags(USER_ID)).thenReturn(List.of());

            List<Tag> resultado = buscarTodasTagsUsecaseImpl.execute(USER_ID);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertTrue(resultado.isEmpty())
            );
            verify(tagRepositoryPort).buscarTodasTags(USER_ID);
        }
    }
}
