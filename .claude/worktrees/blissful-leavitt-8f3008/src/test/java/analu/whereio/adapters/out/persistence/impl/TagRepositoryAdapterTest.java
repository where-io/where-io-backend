package analu.whereio.adapters.out.persistence.impl;

import analu.whereio.adapters.out.persistence.entity.TagEntity;
import analu.whereio.adapters.out.persistence.mapper.TagPersistenceMapper;
import analu.whereio.adapters.out.persistence.repository.TagRepository;
import analu.whereio.application.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagRepositoryAdapter")
class TagRepositoryAdapterTest {

    @Mock
    private TagRepository repository;

    @Mock
    private TagPersistenceMapper mapper;

    @InjectMocks
    private TagRepositoryAdapter tagRepositoryAdapter;

    private Tag tag;
    private TagEntity tagEntity;

    @BeforeEach
    void setUp() {
        tag = new Tag();
        tag.setId("tag-id-1");
        tag.setNome("Italiano");

        tagEntity = new TagEntity();
        tagEntity.setId("tag-id-1");
        tagEntity.setNome("Italiano");
    }

    @Nested
    @DisplayName("cadastrarTag")
    class CadastrarTag {

        @Test
        @DisplayName("deve converter para entity, salvar no repositório e retornar o domínio mapeado")
        void deveCadastrarTagComSucesso() {
            Tag tagSalva = new Tag();
            tagSalva.setId("tag-id-gerado");
            tagSalva.setNome("Italiano");

            when(mapper.toEntity(tag)).thenReturn(tagEntity);
            when(repository.save(tagEntity)).thenReturn(tagEntity);
            when(mapper.toDomain(tagEntity)).thenReturn(tagSalva);

            Tag resultado = tagRepositoryAdapter.cadastrarTag(tag);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals("tag-id-gerado", resultado.getId()),
                    () -> assertEquals("Italiano", resultado.getNome())
            );
            verify(mapper).toEntity(tag);
            verify(repository).save(tagEntity);
            verify(mapper).toDomain(tagEntity);
        }
    }

    @Nested
    @DisplayName("buscarPorIdTag")
    class BuscarPorIdTag {

        @Test
        @DisplayName("deve retornar o domínio mapeado quando o id existe")
        void deveRetornarTagQuandoIdExiste() {
            String id = "tag-id-1";

            when(repository.findById(id)).thenReturn(Optional.of(tagEntity));
            when(mapper.toDomain(tagEntity)).thenReturn(tag);

            Tag resultado = tagRepositoryAdapter.buscarPorIdTag(id);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(id, resultado.getId()),
                    () -> assertEquals("Italiano", resultado.getNome())
            );
            verify(repository).findById(id);
            verify(mapper).toDomain(tagEntity);
        }

        @Test
        @DisplayName("deve retornar null quando o id não existe")
        void deveRetornarNullQuandoIdNaoExiste() {
            String id = "id-inexistente";

            when(repository.findById(id)).thenReturn(Optional.empty());

            Tag resultado = tagRepositoryAdapter.buscarPorIdTag(id);

            assertNull(resultado);
            verify(repository).findById(id);
            verifyNoMoreInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("buscarPorNomeTag")
    class BuscarPorNomeTag {

        @Test
        @DisplayName("deve chamar findByNomeAndUserId e retornar o domínio mapeado quando a tag existe")
        void deveBuscarPorNomeERetornarTag() {
            String nome = "Italiano";
            String userId = "user-1";

            when(repository.findByNomeAndUserId(nome, userId)).thenReturn(tagEntity);
            when(mapper.toDomain(tagEntity)).thenReturn(tag);

            Tag resultado = tagRepositoryAdapter.buscarPorNomeTag(nome, userId);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(nome, resultado.getNome())
            );
            verify(repository).findByNomeAndUserId(nome, userId);
            verify(mapper).toDomain(tagEntity);
        }

        @Test
        @DisplayName("deve retornar null quando não há tag com o nome informado para o usuário")
        void deveRetornarNullQuandoNomeNaoExiste() {
            String nome = "nome-inexistente";
            String userId = "user-1";

            when(repository.findByNomeAndUserId(nome, userId)).thenReturn(null);

            Tag resultado = tagRepositoryAdapter.buscarPorNomeTag(nome, userId);

            assertNull(resultado);
            verify(repository).findByNomeAndUserId(nome, userId);
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("buscarTodasTags")
    class BuscarTodasTags {

        @Test
        @DisplayName("deve retornar lista de domínios mapeados a partir das entities do usuário")
        void deveBuscarTodasTagsERetornarLista() {
            String userId = "user-1";
            TagEntity segundaEntity = new TagEntity();
            segundaEntity.setId("tag-id-2");
            segundaEntity.setNome("Japonês");

            Tag segundaTag = new Tag();
            segundaTag.setId("tag-id-2");
            segundaTag.setNome("Japonês");

            when(repository.findAllByUserId(userId)).thenReturn(List.of(tagEntity, segundaEntity));
            when(mapper.toDomain(tagEntity)).thenReturn(tag);
            when(mapper.toDomain(segundaEntity)).thenReturn(segundaTag);

            List<Tag> resultado = tagRepositoryAdapter.buscarTodasTags(userId);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(2, resultado.size()),
                    () -> assertEquals("tag-id-1", resultado.get(0).getId()),
                    () -> assertEquals("tag-id-2", resultado.get(1).getId())
            );
            verify(repository).findAllByUserId(userId);
            verify(mapper).toDomain(tagEntity);
            verify(mapper).toDomain(segundaEntity);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há tags cadastradas para o usuário")
        void deveRetornarListaVaziaQuandoNaoHaTags() {
            String userId = "user-1";
            when(repository.findAllByUserId(userId)).thenReturn(List.of());

            List<Tag> resultado = tagRepositoryAdapter.buscarTodasTags(userId);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertTrue(resultado.isEmpty())
            );
            verify(repository).findAllByUserId(userId);
            verifyNoMoreInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("atualizarTag")
    class AtualizarTag {

        @Test
        @DisplayName("deve converter para entity e salvar no repositório sem retornar valor")
        void deveAtualizarTagComSucesso() {
            when(mapper.toEntity(tag)).thenReturn(tagEntity);

            tagRepositoryAdapter.atualizarTag(tag);

            verify(mapper).toEntity(tag);
            verify(repository).save(tagEntity);
        }
    }

    @Nested
    @DisplayName("removerTagPorId")
    class RemoverTagPorId {

        @Test
        @DisplayName("deve chamar deleteById com o id informado")
        void deveRemoverTagPorIdComSucesso() {
            String id = "tag-id-1";

            tagRepositoryAdapter.removerTagPorId(id);

            verify(repository).deleteById(id);
            verifyNoInteractions(mapper);
        }
    }
}
