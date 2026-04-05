package analu.whereio.adapters.out.persistence.impl;

import analu.whereio.adapters.out.persistence.entity.VisitaEntity;
import analu.whereio.adapters.out.persistence.mapper.VisitaPersistanceMapper;
import analu.whereio.adapters.out.persistence.repository.VisitaRepository;
import analu.whereio.application.model.Visita;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VisitaRepositoryAdapter")
class VisitaRepositoryAdapterTest {

    @Mock
    private VisitaRepository repository;

    @Mock
    private VisitaPersistanceMapper mapper;

    @InjectMocks
    private VisitaRepositoryAdapter visitaRepositoryAdapter;

    private Visita visita;
    private VisitaEntity visitaEntity;

    @BeforeEach
    void setUp() {
        visita = new Visita();
        visita.setId("visita-id-1");
        visita.setDataVisita(LocalDate.of(2024, 6, 15));
        visita.setAvaliacao(4);
        visita.setComentario("Ótimo lugar!");
        visita.setIdLocal("local-id-1");

        visitaEntity = new VisitaEntity();
        visitaEntity.setId("visita-id-1");
        visitaEntity.setDataVisita(LocalDate.of(2024, 6, 15));
        visitaEntity.setAvaliacao(4);
        visitaEntity.setComentario("Ótimo lugar!");
        visitaEntity.setIdLocal("local-id-1");
    }

    @Nested
    @DisplayName("adicionarVisita")
    class AdicionarVisita {

        @Test
        @DisplayName("deve converter para entity, salvar e retornar o id da entity salva")
        void deveAdicionarVisitaERetornarId() {
            VisitaEntity entitySalva = new VisitaEntity();
            entitySalva.setId("visita-id-gerada");

            when(mapper.toEntity(visita)).thenReturn(visitaEntity);
            when(repository.save(visitaEntity)).thenReturn(entitySalva);

            String idRetornado = visitaRepositoryAdapter.adicionarVisita(visita);

            assertEquals("visita-id-gerada", idRetornado);
            verify(mapper).toEntity(visita);
            verify(repository).save(visitaEntity);
        }
    }

    @Nested
    @DisplayName("removerVisita")
    class RemoverVisita {

        @Test
        @DisplayName("deve chamar deleteById com o id informado")
        void deveRemoverVisitaPorIdComSucesso() {
            String id = "visita-id-1";

            visitaRepositoryAdapter.removerVisita(id);

            verify(repository).deleteById(id);
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("atualizarVisita")
    class AtualizarVisita {

        @Test
        @DisplayName("deve converter para entity e salvar no repositório sem retornar valor")
        void deveAtualizarVisitaComSucesso() {
            when(mapper.toEntity(visita)).thenReturn(visitaEntity);

            visitaRepositoryAdapter.atualizarVisita(visita);

            verify(mapper).toEntity(visita);
            verify(repository).save(visitaEntity);
        }
    }

    @Nested
    @DisplayName("buscarVisitasPorIdLocal")
    class BuscarVisitasPorIdLocal {

        @Test
        @DisplayName("deve retornar lista de domínios mapeados quando há visitas para o local")
        void deveRetornarListaDeVisitasDoLocal() {
            String idLocal = "local-id-1";

            VisitaEntity segundaEntity = new VisitaEntity();
            segundaEntity.setId("visita-id-2");
            segundaEntity.setIdLocal(idLocal);

            Visita segundaVisita = new Visita();
            segundaVisita.setId("visita-id-2");
            segundaVisita.setIdLocal(idLocal);

            when(repository.findByIdLocal(idLocal)).thenReturn(List.of(visitaEntity, segundaEntity));
            when(mapper.toDomain(visitaEntity)).thenReturn(visita);
            when(mapper.toDomain(segundaEntity)).thenReturn(segundaVisita);

            List<Visita> resultado = visitaRepositoryAdapter.buscarVisitasPorIdLocal(idLocal);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(2, resultado.size()),
                    () -> assertEquals("visita-id-1", resultado.get(0).getId()),
                    () -> assertEquals("visita-id-2", resultado.get(1).getId())
            );
            verify(repository).findByIdLocal(idLocal);
            verify(mapper).toDomain(visitaEntity);
            verify(mapper).toDomain(segundaEntity);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há visitas para o local")
        void deveRetornarListaVaziaQuandoNaoHaVisitas() {
            String idLocal = "local-sem-visitas";

            when(repository.findByIdLocal(idLocal)).thenReturn(List.of());

            List<Visita> resultado = visitaRepositoryAdapter.buscarVisitasPorIdLocal(idLocal);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertTrue(resultado.isEmpty())
            );
            verify(repository).findByIdLocal(idLocal);
            verifyNoMoreInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar o domínio mapeado quando o id existe")
        void deveRetornarVisitaQuandoIdExiste() {
            String id = "visita-id-1";

            when(repository.findById(id)).thenReturn(Optional.of(visitaEntity));
            when(mapper.toDomain(visitaEntity)).thenReturn(visita);

            Visita resultado = visitaRepositoryAdapter.buscarPorId(id);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(id, resultado.getId()),
                    () -> assertEquals("local-id-1", resultado.getIdLocal())
            );
            verify(repository).findById(id);
            verify(mapper).toDomain(visitaEntity);
        }

        @Test
        @DisplayName("deve retornar null quando o id não existe")
        void deveRetornarNullQuandoIdNaoExiste() {
            String id = "id-inexistente";

            when(repository.findById(id)).thenReturn(Optional.empty());

            Visita resultado = visitaRepositoryAdapter.buscarPorId(id);

            assertNull(resultado);
            verify(repository).findById(id);
            verifyNoInteractions(mapper);
        }
    }
}
