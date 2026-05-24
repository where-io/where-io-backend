package analu.whereio.adapters.in.web.converter;

import analu.whereio.adapters.in.web.dto.response.AmigoPerfilResponse;
import analu.whereio.adapters.in.web.dto.response.AmizadeConviteResponse;
import analu.whereio.adapters.in.web.dto.response.ConviteEnviadoResponse;
import analu.whereio.application.model.ConviteAmizadeResumoItem;
import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.FriendshipStatus;
import analu.whereio.application.model.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AmigosConverter {

    @Mapping(source = "requesterUserId", target = "solicitanteId")
    @Mapping(source = "addresseeUserId", target = "destinatarioId")
    @Mapping(source = "createdAt", target = "criadoEm")
    @Mapping(source = "acceptedAt", target = "aceitoEm")
    @Mapping(source = "status", target = "status", qualifiedByName = "friendshipStatusToString")
    AmizadeConviteResponse toConviteResponse(Friendship friendship);

    @Mapping(source = "createdAt", target = "criadoEm")
    @Mapping(source = "acceptedAt", target = "aceitoEm")
    @Mapping(source = "status", target = "status", qualifiedByName = "friendshipStatusToString")
    ConviteEnviadoResponse toConviteEnviadoResponse(ConviteAmizadeResumoItem item);

    @Named("friendshipStatusToString")
    default String friendshipStatusToString(FriendshipStatus status) {
        return status == null ? null : status.name();
    }

    @Mapping(source = "id", target = "id")
    @Mapping(source = "nomeUsuario", target = "nomeUsuario")
    @Mapping(source = "nome", target = "nome")
    @Mapping(target = "fotoPerfilUrl", ignore = true)
    AmigoPerfilResponse toAmigoResponse(UserAccount account);
}
