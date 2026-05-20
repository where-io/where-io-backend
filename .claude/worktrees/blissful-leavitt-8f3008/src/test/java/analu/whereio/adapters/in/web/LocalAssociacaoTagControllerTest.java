package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.TagConverter;
import analu.whereio.adapters.in.web.dto.response.TagDtoResponse;
import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.in.tag.AssociarTagLocalUsecase;
import analu.whereio.application.ports.in.tag.BuscarTagsPorLocalUsecase;
import analu.whereio.application.ports.in.tag.RemoverAssociacaoTagLocalUsecase;
import analu.whereio.config.security.JwtUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocalAssociacaoTagController")
class LocalAssociacaoTagControllerTest {

    private static final JwtUserPrincipal PRINCIPAL = JwtUserPrincipal.testPrincipal("user-1");

    @Mock
    private AssociarTagLocalUsecase assocTagLocalUsecase;

    @Mock
    private RemoverAssociacaoTagLocalUsecase removerAssocTagLocalUsecase;

    @Mock
    private BuscarTagsPorLocalUsecase buscarTagsPorLocalUsecase;

    @Mock
    private TagConverter mapper;

    @InjectMocks
    private LocalAssociacaoTagController localTagController;

    private Tag tag;
    private TagDtoResponse tagDtoResponse;

    @BeforeEach
    void setUp() {
        tag = new Tag();
        tag.setId("tag-id-1");
        tag.setNome("Italiano");

        tagDtoResponse = new TagDtoResponse();
        tagDtoResponse.setId("tag-id-1");
        tagDtoResponse.setNome("Italiano");
    }

    @Nested
    @DisplayName("POST /api/local/{idLocal}/tag/{idTag} - associarTagAoLocal")
    class AssociarTagAoLocal {

        @Test
        @DisplayName("deve executar o use case de associação com idLocal e idTag e retornar 200 OK sem body")
        void deveAssociarTagAoLocalERetornar200SemBody() {
            String idLocal = "local-id-1";
            String idTag = "tag-id-1";

            ResponseEntity<Void> resposta = localTagController.associarTagAoLocal(PRINCIPAL, idLocal, idTag);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertNull(resposta.getBody())
            );
            verify(assocTagLocalUsecase).execute(idLocal, idTag, "user-1");
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("DELETE /api/local/{idLocal}/tag/{idTag} - removerAssociacaoTagDoLocal")
    class RemoverAssociacaoTagDoLocal {

        @Test
        @DisplayName("deve executar o use case de remoção de associação com idLocal e idTag e retornar 200 OK sem body")
        void deveRemoverAssociacaoTagDoLocalERetornar200SemBody() {
            String idLocal = "local-id-1";
            String idTag = "tag-id-1";

            ResponseEntity<Void> resposta = localTagController.removerAssociacaoTagDoLocal(PRINCIPAL, idLocal, idTag);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertNull(resposta.getBody())
            );
            verify(removerAssocTagLocalUsecase).execute(idLocal, idTag, "user-1");
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("GET /api/local/{idLocal}/tags - buscarTagsDoLocal")
    class BuscarTagsDoLocal {

        @Test
        @DisplayName("deve executar o use case, mapear cada tag e retornar 200 OK com a lista de responses")
        void deveBuscarTagsDoLocalERetornarListaCom200() {
            String idLocal = "local-id-1";

            Tag segundaTag = new Tag();
            segundaTag.setId("tag-id-2");
            segundaTag.setNome("Japonês");

            TagDtoResponse segundaResponse = new TagDtoResponse();
            segundaResponse.setId("tag-id-2");
            segundaResponse.setNome("Japonês");

            when(buscarTagsPorLocalUsecase.execute(idLocal, "user-1")).thenReturn(List.of(tag, segundaTag));
            when(mapper.toResponse(tag)).thenReturn(tagDtoResponse);
            when(mapper.toResponse(segundaTag)).thenReturn(segundaResponse);

            ResponseEntity<List<TagDtoResponse>> resposta = localTagController.buscarTagsDoLocal(PRINCIPAL, idLocal);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertNotNull(resposta.getBody()),
                    () -> assertEquals(2, resposta.getBody().size()),
                    () -> assertEquals("tag-id-1", resposta.getBody().get(0).getId()),
                    () -> assertEquals("tag-id-2", resposta.getBody().get(1).getId())
            );
            verify(buscarTagsPorLocalUsecase).execute(idLocal, "user-1");
            verify(mapper).toResponse(tag);
            verify(mapper).toResponse(segundaTag);
        }

        @Test
        @DisplayName("deve retornar 200 OK com lista vazia quando o local não possui tags associadas")
        void deveRetornarListaVaziaQuandoLocalNaoPossuiTags() {
            String idLocal = "local-sem-tags";

            when(buscarTagsPorLocalUsecase.execute(idLocal, "user-1")).thenReturn(List.of());

            ResponseEntity<List<TagDtoResponse>> resposta = localTagController.buscarTagsDoLocal(PRINCIPAL, idLocal);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertNotNull(resposta.getBody()),
                    () -> assertTrue(resposta.getBody().isEmpty())
            );
            verify(buscarTagsPorLocalUsecase).execute(idLocal, "user-1");
            verifyNoMoreInteractions(mapper);
        }
    }
}
