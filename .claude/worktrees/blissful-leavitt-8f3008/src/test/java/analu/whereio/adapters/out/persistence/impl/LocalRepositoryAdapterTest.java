package analu.whereio.adapters.out.persistence.impl;

import analu.whereio.adapters.out.persistence.entity.LocalEntity;
import analu.whereio.adapters.out.persistence.mapper.LocalPersistenceMapper;
import analu.whereio.adapters.out.persistence.repository.LocalRepository;
import analu.whereio.application.model.Coordenadas;
import analu.whereio.application.model.Endereco;
import analu.whereio.application.model.Local;
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
@DisplayName("LocalRepositoryAdapter")
class LocalRepositoryAdapterTest {

    @Mock
    private LocalRepository repository;

    @Mock
    private LocalPersistenceMapper mapper;

    @InjectMocks
    private LocalRepositoryAdapter localRepositoryAdapter;

    private Local local;
    private LocalEntity localEntity;

    @BeforeEach
    void setUp() {
        Endereco endereco = new Endereco();
        endereco.setLogradouro("Rua das Flores");
        endereco.setBairro("Centro");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");
        endereco.setCep("01310-100");
        endereco.setPais("Brasil");

        Coordenadas coordenadas = Coordenadas.builder()
                .latitude("-23.5505")
                .longitude("-46.6333")
                .build();

        local = new Local();
        local.setId("local-id-1");
        local.setNome("Restaurante Bom Sabor");
        local.setEndereco(endereco);
        local.setCoordenadas(coordenadas);

        localEntity = new LocalEntity();
        localEntity.setId("local-id-1");
        localEntity.setNome("Restaurante Bom Sabor");
        localEntity.setEndereco(endereco);
        localEntity.setCoordenadas(coordenadas);
    }

    @Nested
    @DisplayName("cadastrarLocal")
    class CadastrarLocal {

        @Test
        @DisplayName("deve converter para entity, salvar no repositório e retornar o domínio mapeado")
        void deveCadastrarLocalComSucesso() {
            Local localSalvo = new Local();
            localSalvo.setId("local-id-gerado");
            localSalvo.setNome("Restaurante Bom Sabor");

            when(mapper.toEntity(local)).thenReturn(localEntity);
            when(repository.save(localEntity)).thenReturn(localEntity);
            when(mapper.toDomain(localEntity)).thenReturn(localSalvo);

            Local resultado = localRepositoryAdapter.cadastrarLocal(local);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals("local-id-gerado", resultado.getId()),
                    () -> assertEquals("Restaurante Bom Sabor", resultado.getNome())
            );
            verify(mapper).toEntity(local);
            verify(repository).save(localEntity);
            verify(mapper).toDomain(localEntity);
        }
    }

    @Nested
    @DisplayName("buscarPorNomeLocal")
    class BuscarPorNomeLocal {

        @Test
        @DisplayName("deve chamar findByNomeAndOwnerUserId e retornar o domínio mapeado")
        void deveBuscarPorNomeERetornarLocal() {
            String nome = "Restaurante Bom Sabor";
            String ownerUserId = "user-1";

            when(repository.findByNomeAndOwnerUserId(nome, ownerUserId)).thenReturn(localEntity);
            when(mapper.toDomain(localEntity)).thenReturn(local);

            Local resultado = localRepositoryAdapter.buscarPorNomeLocal(nome, ownerUserId);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(nome, resultado.getNome())
            );
            verify(repository).findByNomeAndOwnerUserId(nome, ownerUserId);
            verify(mapper).toDomain(localEntity);
        }
    }

    @Nested
    @DisplayName("buscarTodosLocalPorUsuario")
    class BuscarTodosLocalPorUsuario {

        @Test
        @DisplayName("deve retornar lista de domínios mapeados a partir das entities do usuário")
        void deveBuscarTodosLocaisERetornarLista() {
            String ownerUserId = "user-1";
            LocalEntity segundaEntity = new LocalEntity();
            segundaEntity.setId("local-id-2");
            segundaEntity.setNome("Bar do João");

            Local segundoLocal = new Local();
            segundoLocal.setId("local-id-2");
            segundoLocal.setNome("Bar do João");

            when(repository.findAllByOwnerUserId(ownerUserId)).thenReturn(List.of(localEntity, segundaEntity));
            when(mapper.toDomain(localEntity)).thenReturn(local);
            when(mapper.toDomain(segundaEntity)).thenReturn(segundoLocal);

            List<Local> resultado = localRepositoryAdapter.buscarTodosLocalPorUsuario(ownerUserId);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(2, resultado.size()),
                    () -> assertEquals("local-id-1", resultado.get(0).getId()),
                    () -> assertEquals("local-id-2", resultado.get(1).getId())
            );
            verify(repository).findAllByOwnerUserId(ownerUserId);
            verify(mapper).toDomain(localEntity);
            verify(mapper).toDomain(segundaEntity);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há locais cadastrados para o usuário")
        void deveRetornarListaVaziaQuandoNaoHaLocais() {
            String ownerUserId = "user-1";
            when(repository.findAllByOwnerUserId(ownerUserId)).thenReturn(List.of());

            List<Local> resultado = localRepositoryAdapter.buscarTodosLocalPorUsuario(ownerUserId);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertTrue(resultado.isEmpty())
            );
            verify(repository).findAllByOwnerUserId(ownerUserId);
            verifyNoMoreInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("buscarPorIdLocal")
    class BuscarPorIdLocal {

        @Test
        @DisplayName("deve retornar o domínio mapeado quando o id existe")
        void deveRetornarLocalQuandoIdExiste() {
            String id = "local-id-1";

            when(repository.findById(id)).thenReturn(Optional.of(localEntity));
            when(mapper.toDomain(localEntity)).thenReturn(local);

            Local resultado = localRepositoryAdapter.buscarPorIdLocal(id);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(id, resultado.getId())
            );
            verify(repository).findById(id);
            verify(mapper).toDomain(localEntity);
        }

        @Test
        @DisplayName("deve retornar null quando o id não existe")
        void deveRetornarNullQuandoIdNaoExiste() {
            String id = "id-inexistente";

            when(repository.findById(id)).thenReturn(Optional.empty());

            Local resultado = localRepositoryAdapter.buscarPorIdLocal(id);

            assertNull(resultado);
            verify(repository).findById(id);
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("atualizarLocal")
    class AtualizarLocal {

        @Test
        @DisplayName("deve converter para entity e salvar no repositório sem retornar valor")
        void deveAtualizarLocalComSucesso() {
            when(mapper.toEntity(local)).thenReturn(localEntity);

            localRepositoryAdapter.atualizarLocal(local);

            verify(mapper).toEntity(local);
            verify(repository).save(localEntity);
        }
    }

    @Nested
    @DisplayName("removerLocalPorId")
    class RemoverLocalPorId {

        @Test
        @DisplayName("deve chamar deleteById com o id informado")
        void deveRemoverLocalPorIdComSucesso() {
            String id = "local-id-1";

            localRepositoryAdapter.removerLocalPorId(id);

            verify(repository).deleteById(id);
            verifyNoInteractions(mapper);
        }
    }
}
