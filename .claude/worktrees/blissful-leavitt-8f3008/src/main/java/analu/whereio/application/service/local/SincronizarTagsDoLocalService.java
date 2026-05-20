package analu.whereio.application.service.local;

import analu.whereio.application.model.Categoria;
import analu.whereio.application.model.Local;
import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.out.TagRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converte {@link Local#getTags()} (payload) e referências em {@link Local#getIdTags()}
 * em uma lista definitiva de IDs persistidos em {@code tag_table}, sempre no escopo do {@link Local#getOwnerUserId()}.
 */
@Component
@RequiredArgsConstructor
public class SincronizarTagsDoLocalService {

    private final TagRepositoryPort tagRepositoryPort;

    public void aplicar(Local local) {
        String ownerUserId = local.getOwnerUserId();
        if (ownerUserId == null || ownerUserId.isBlank()) {
            throw new IllegalStateException("ownerUserId é obrigatório para sincronizar tags do local");
        }

        Map<String, String> corPorNomeLower = montarCorPorNome(local);

        LinkedHashSet<String> ids = new LinkedHashSet<>();
        if (local.getTags() != null) {
            for (Categoria c : local.getTags()) {
                if (c == null || c.getNome() == null || c.getNome().isBlank()) {
                    continue;
                }
                String nome = c.getNome().trim();
                Tag existente = tagRepositoryPort.buscarPorNomeTag(nome, ownerUserId);
                if (existente != null) {
                    aplicarCorSeInformado(existente, c.getCor(), ownerUserId);
                    ids.add(existente.getId());
                } else {
                    Tag nova = new Tag();
                    nova.setNome(nome);
                    nova.setCor(c.getCor());
                    nova.setUserId(ownerUserId);
                    ids.add(tagRepositoryPort.cadastrarTag(nova).getId());
                }
            }
        }
        if (local.getIdTags() != null) {
            for (String raw : local.getIdTags()) {
                if (raw == null || raw.isBlank()) {
                    continue;
                }
                String trimmed = raw.trim();
                if (ids.contains(trimmed)) {
                    continue;
                }
                Tag porId = tagRepositoryPort.buscarPorIdTagDoUsuario(trimmed, ownerUserId);
                if (porId != null) {
                    ids.add(porId.getId());
                    continue;
                }
                Tag porNome = tagRepositoryPort.buscarPorNomeTag(trimmed, ownerUserId);
                if (porNome != null) {
                    aplicarCorSeInformado(porNome, corPorNomeLower.get(trimmed.toLowerCase(Locale.ROOT)), ownerUserId);
                    ids.add(porNome.getId());
                } else {
                    Tag nova = new Tag();
                    nova.setNome(trimmed);
                    nova.setCor(corPorNomeLower.get(trimmed.toLowerCase(Locale.ROOT)));
                    nova.setUserId(ownerUserId);
                    ids.add(tagRepositoryPort.cadastrarTag(nova).getId());
                }
            }
        }
        local.setIdTags(new ArrayList<>(ids));
        local.setTags(new ArrayList<>());
    }

    private static Map<String, String> montarCorPorNome(Local local) {
        Map<String, String> map = new LinkedHashMap<>();
        if (local.getTags() == null) {
            return map;
        }
        for (Categoria c : local.getTags()) {
            if (c == null || c.getNome() == null || c.getNome().isBlank()) {
                continue;
            }
            if (c.getCor() == null || c.getCor().isBlank()) {
                continue;
            }
            map.putIfAbsent(c.getNome().trim().toLowerCase(Locale.ROOT), c.getCor().trim());
        }
        return map;
    }

    private void aplicarCorSeInformado(Tag tag, String novaCor, String ownerUserId) {
        if (tag == null || novaCor == null || novaCor.isBlank()) {
            return;
        }
        if (tag.getUserId() != null && !tag.getUserId().equals(ownerUserId)) {
            return;
        }
        String trimmed = novaCor.trim();
        String atual = tag.getCor() != null ? tag.getCor().trim() : "";
        if (trimmed.equals(atual)) {
            return;
        }
        tag.setCor(trimmed);
        tagRepositoryPort.atualizarTag(tag);
    }

    /** Preenche {@link Local#getTags()} com {@link Categoria} (id, nome, cor) a partir de {@link Local#getIdTags()}. */
    public void hidratarParaResposta(Local local) {
        String ownerUserId = local.getOwnerUserId();
        if (local.getIdTags() == null || local.getIdTags().isEmpty()) {
            local.setTags(new ArrayList<>());
            return;
        }

        List<String> idsUnicos = local.getIdTags().stream()
                .filter(id -> id != null && !id.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        List<Tag> tags;
        if (ownerUserId != null && !ownerUserId.isBlank()) {
            tags = tagRepositoryPort.buscarPorIds(idsUnicos, ownerUserId);
        } else {
            tags = idsUnicos.stream()
                    .map(tagRepositoryPort::buscarPorIdTag)
                    .filter(Objects::nonNull)
                    .toList();
        }

        Map<String, Tag> tagPorId = tags.stream()
                .collect(Collectors.toMap(Tag::getId, Function.identity()));

        List<Categoria> vistas = idsUnicos.stream()
                .map(tagPorId::get)
                .filter(Objects::nonNull)
                .map(tag -> {
                    Categoria c = new Categoria();
                    c.setId(tag.getId());
                    c.setNome(tag.getNome());
                    c.setCor(tag.getCor());
                    return c;
                })
                .toList();

        local.setTags(vistas);
    }
}
