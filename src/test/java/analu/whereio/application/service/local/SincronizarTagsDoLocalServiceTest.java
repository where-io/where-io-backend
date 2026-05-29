package analu.whereio.application.service.local;

import analu.whereio.application.model.Categoria;
import analu.whereio.application.model.Local;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SincronizarTagsDoLocalService")
class SincronizarTagsDoLocalServiceTest {

    private static final String OWNER_USER_ID = "owner-1";

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    @InjectMocks
    private SincronizarTagsDoLocalService service;

    private Local local;

    @BeforeEach
    void setUp() {
        local = new Local();
        local.setId("local-1");
        local.setOwnerUserId(OWNER_USER_ID);
    }

    @Nested
    @DisplayName("aplicar")
    class Aplicar {

        @Test
        @DisplayName("deve lançar IllegalStateException quando ownerUserId é nulo")
        void deveLancarQuandoOwnerUserIdNulo() {
            // TODO: scaffold — ownerUserId == null throws IllegalStateException
            // local.setOwnerUserId(null)
            // Assert: assertThrows(IllegalStateException.class, () -> service.aplicar(local))
            // Verify: verifyNoInteractions(tagRepositoryPort)
        }

        @Test
        @DisplayName("deve lançar IllegalStateException quando ownerUserId é em branco")
        void deveLancarQuandoOwnerUserIdEmBranco() {
            // TODO: scaffold — ownerUserId.isBlank() throws IllegalStateException
            // local.setOwnerUserId("   ")
            // Assert: assertThrows(IllegalStateException.class, () -> service.aplicar(local))
            // Verify: verifyNoInteractions(tagRepositoryPort)
        }

        @Test
        @DisplayName("cria tags novas a partir de Categoria e preenche idTags")
        void criaTagsNovas() {
            Categoria cat = new Categoria();
            cat.setNome("Italiano");
            cat.setCor("#112233");
            local.setTags(List.of(cat));
            local.setIdTags(List.of());

            when(tagRepositoryPort.buscarPorNomeTag("Italiano", OWNER_USER_ID)).thenReturn(null);
            Tag salva = new Tag();
            salva.setId("tid-1");
            salva.setNome("Italiano");
            salva.setCor("#112233");
            when(tagRepositoryPort.cadastrarTag(any(Tag.class))).thenReturn(salva);

            service.aplicar(local);

            assertEquals(List.of("tid-1"), local.getIdTags());
            assertTrue(local.getTags().isEmpty());
            verify(tagRepositoryPort).cadastrarTag(argThat(t ->
                    "Italiano".equals(t.getNome()) && "#112233".equals(t.getCor()) && OWNER_USER_ID.equals(t.getUserId())));
        }

        @Test
        @DisplayName("reutiliza tag existente por nome e não duplica quando idTags repete o nome")
        void reutilizaPorNome() {
            Tag existente = new Tag();
            existente.setId("tid-x");
            existente.setNome("Bar");
            local.setTags(List.of());
            local.setIdTags(List.of("Bar", "Bar"));

            when(tagRepositoryPort.buscarPorIdTagDoUsuario("Bar", OWNER_USER_ID)).thenReturn(null);
            when(tagRepositoryPort.buscarPorNomeTag("Bar", OWNER_USER_ID)).thenReturn(existente);

            service.aplicar(local);

            assertEquals(List.of("tid-x"), local.getIdTags());
            verify(tagRepositoryPort, never()).cadastrarTag(any());
        }

        @Test
        @DisplayName("atualiza cor no banco quando a tag já existe por nome e o payload traz cor nova")
        void atualizaCorTagExistente() {
            Categoria cat = new Categoria();
            cat.setNome("Italiano");
            cat.setCor("#ABCDEF");
            local.setTags(List.of(cat));

            Tag existente = new Tag();
            existente.setId("tid-old");
            existente.setNome("Italiano");
            existente.setCor(null);
            existente.setUserId(OWNER_USER_ID);
            when(tagRepositoryPort.buscarPorNomeTag("Italiano", OWNER_USER_ID)).thenReturn(existente);

            service.aplicar(local);

            assertEquals(List.of("tid-old"), local.getIdTags());
            verify(tagRepositoryPort, never()).cadastrarTag(any());
            verify(tagRepositoryPort).atualizarTag(argThat(t ->
                    "tid-old".equals(t.getId()) && "#ABCDEF".equals(t.getCor())));
        }

        @Test
        @DisplayName("aceita idTags como id Mongo quando a tag existe")
        void aceitaIdMongo() {
            Tag porId = new Tag();
            porId.setId("507f1f77bcf86cd799439011");
            porId.setNome("Pizza");
            local.setTags(new ArrayList<>());
            local.setIdTags(List.of("507f1f77bcf86cd799439011"));

            when(tagRepositoryPort.buscarPorIdTagDoUsuario("507f1f77bcf86cd799439011", OWNER_USER_ID)).thenReturn(porId);

            service.aplicar(local);

            assertEquals(List.of("507f1f77bcf86cd799439011"), local.getIdTags());
            verify(tagRepositoryPort, never()).cadastrarTag(any());
        }
    }

    @Nested
    @DisplayName("hidratarParaResposta")
    class Hidratar {

        @Test
        @DisplayName("monta lista de Categoria com id a partir de idTags")
        void montaCategorias() {
            local.setIdTags(List.of("a", "b"));
            Tag t1 = new Tag();
            t1.setId("a");
            t1.setNome("A");
            t1.setCor("#111");
            Tag t2 = new Tag();
            t2.setId("b");
            t2.setNome("B");
            t2.setCor(null);
            when(tagRepositoryPort.buscarPorIds(List.of("a", "b"), OWNER_USER_ID)).thenReturn(List.of(t1, t2));

            service.hidratarParaResposta(local);

            assertEquals(2, local.getTags().size());
            assertEquals("a", local.getTags().get(0).getId());
            assertEquals("A", local.getTags().get(0).getNome());
            assertEquals("#111", local.getTags().get(0).getCor());
            assertEquals("b", local.getTags().get(1).getId());
        }

        @Test
        @DisplayName("lista vazia quando não há idTags")
        void semIdTags() {
            local.setIdTags(List.of());
            service.hidratarParaResposta(local);
            assertTrue(local.getTags().isEmpty());
        }
    }
}
