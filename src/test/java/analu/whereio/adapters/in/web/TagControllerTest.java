package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.TagConverter;
import analu.whereio.adapters.in.web.dto.request.TagDtoRequest;
import analu.whereio.adapters.in.web.dto.response.TagDtoResponse;
import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.in.tag.AtualizarTagUsecase;
import analu.whereio.application.ports.in.tag.BuscarTagPorIdUsecase;
import analu.whereio.application.ports.in.tag.BuscarTodasTagsUsecase;
import analu.whereio.application.ports.in.tag.CadastrarTagUsecase;
import analu.whereio.application.ports.in.tag.RemoverTagUsecase;
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
@DisplayName("TagController")
class TagControllerTest {

    private static final JwtUserPrincipal PRINCIPAL = JwtUserPrincipal.testPrincipal("user-1");

    @Mock
    private CadastrarTagUsecase cadastrarTagUsecase;

    @Mock
    private AtualizarTagUsecase atualizarTagUsecase;

    @Mock
    private BuscarTagPorIdUsecase buscarTagPorIdUsecase;

    @Mock
    private BuscarTodasTagsUsecase buscarTodasTagsUsecase;

    @Mock
    private RemoverTagUsecase removerTagUsecase;

    @Mock
    private TagConverter mapper;

    @InjectMocks
    private TagController tagController;

    private Tag tag;
    private TagDtoRequest tagDtoRequest;
    private TagDtoResponse tagDtoResponse;

    @BeforeEach
    void setUp() {
        tag = new Tag();
        tag.setId("tag-id-1");
        tag.setNome("Italiano");

        tagDtoRequest = TagDtoRequest.builder()
                .nome("Italiano")
                .build();

        tagDtoResponse = new TagDtoResponse();
        tagDtoResponse.setId("tag-id-1");
        tagDtoResponse.setNome("Italiano");
    }

    @Nested
    @DisplayName("POST /api/tag - cadastrarTag")
    class CadastrarTag {

        @Test
        @DisplayName("deve converter o request para domínio, executar o use case, mapear a resposta e retornar 200 OK com o id")
        void deveCadastrarTagERetornarIdCom200() {
            when(mapper.toDomain(tagDtoRequest)).thenReturn(tag);
            when(cadastrarTagUsecase.execute(tag)).thenReturn(tag);
            when(mapper.toResponse(tag)).thenReturn(tagDtoResponse);

            ResponseEntity<String> resposta = tagController.cadastrarTag(PRINCIPAL, tagDtoRequest);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertEquals("tag-id-1", resposta.getBody()),
                    () -> assertEquals("user-1", tag.getUserId())
            );
            verify(mapper).toDomain(tagDtoRequest);
            verify(cadastrarTagUsecase).execute(tag);
            verify(mapper).toResponse(tag);
        }
    }

    @Nested
    @DisplayName("GET /api/tag/all - buscarTodasTags")
    class BuscarTodasTags {

        @Test
        @DisplayName("deve executar o use case, mapear cada tag e retornar 200 OK com a lista de responses")
        void deveBuscarTodasTagsERetornarListaCom200() {
            Tag segundaTag = new Tag();
            segundaTag.setId("tag-id-2");
            segundaTag.setNome("Japonês");

            TagDtoResponse segundaResponse = new TagDtoResponse();
            segundaResponse.setId("tag-id-2");
            segundaResponse.setNome("Japonês");

            when(buscarTodasTagsUsecase.execute("user-1")).thenReturn(List.of(tag, segundaTag));
            when(mapper.toResponse(tag)).thenReturn(tagDtoResponse);
            when(mapper.toResponse(segundaTag)).thenReturn(segundaResponse);

            ResponseEntity<List<TagDtoResponse>> resposta = tagController.buscarTodasTags(PRINCIPAL);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertNotNull(resposta.getBody()),
                    () -> assertEquals(2, resposta.getBody().size()),
                    () -> assertEquals("tag-id-1", resposta.getBody().get(0).getId()),
                    () -> assertEquals("tag-id-2", resposta.getBody().get(1).getId())
            );
            verify(buscarTodasTagsUsecase).execute("user-1");
            verify(mapper).toResponse(tag);
            verify(mapper).toResponse(segundaTag);
        }

        @Test
        @DisplayName("deve retornar 200 OK com lista vazia quando não há tags cadastradas")
        void deveRetornarListaVaziaQuandoNaoHaTags() {
            when(buscarTodasTagsUsecase.execute("user-1")).thenReturn(List.of());

            ResponseEntity<List<TagDtoResponse>> resposta = tagController.buscarTodasTags(PRINCIPAL);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertNotNull(resposta.getBody()),
                    () -> assertTrue(resposta.getBody().isEmpty())
            );
            verify(buscarTodasTagsUsecase).execute("user-1");
            verifyNoMoreInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("GET /api/tag/{id} - buscarTagPorId")
    class BuscarTagPorId {

        @Test
        @DisplayName("deve executar o use case com o id, mapear a resposta e retornar 200 OK com o response")
        void deveBuscarTagPorIdERetornarResponseCom200() {
            String id = "tag-id-1";

            when(buscarTagPorIdUsecase.execute(id, "user-1")).thenReturn(tag);
            when(mapper.toResponse(tag)).thenReturn(tagDtoResponse);

            ResponseEntity<TagDtoResponse> resposta = tagController.buscarTagPorId(PRINCIPAL, id);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertSame(tagDtoResponse, resposta.getBody())
            );
            verify(buscarTagPorIdUsecase).execute(id, "user-1");
            verify(mapper).toResponse(tag);
        }
    }

    @Nested
    @DisplayName("PUT /api/tag/{id} - atualizarTag")
    class AtualizarTag {

        @Test
        @DisplayName("deve converter o request, executar o use case de atualização com id e tag e retornar 200 OK sem body")
        void deveAtualizarTagERetornar200SemBody() {
            String id = "tag-id-1";

            when(mapper.toDomain(tagDtoRequest)).thenReturn(tag);

            ResponseEntity<Void> resposta = tagController.atualizarTag(PRINCIPAL, id, tagDtoRequest);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertNull(resposta.getBody())
            );
            verify(mapper).toDomain(tagDtoRequest);
            verify(atualizarTagUsecase).execute(tag, id, "user-1");
        }
    }

    @Nested
    @DisplayName("DELETE /api/tag/{id} - removerTag")
    class RemoverTag {

        @Test
        @DisplayName("deve executar o use case de remoção e retornar 200 OK sem body")
        void deveRemoverTagERetornar200SemBody() {
            String id = "tag-id-1";

            ResponseEntity<Void> resposta = tagController.removerTag(PRINCIPAL, id);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertNull(resposta.getBody())
            );
            verify(removerTagUsecase).execute(id, "user-1");
            verifyNoInteractions(mapper);
        }
    }
}
