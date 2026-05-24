package analu.whereio.adapters.in.web.converter;

import analu.whereio.adapters.in.web.dto.response.UsuarioBuscaResponse;
import analu.whereio.adapters.in.web.dto.response.UsuarioPerfilResponse;
import analu.whereio.application.model.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserConverter {

    @Mapping(source = "nomeUsuario", target = "nomeUsuario")
    @Mapping(source = "nome", target = "nome")
    @Mapping(target = "fotoPerfilUrl", ignore = true)
    UsuarioBuscaResponse toBuscaResponse(UserAccount account);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "nome", target = "nome")
    @Mapping(source = "nomeUsuario", target = "nomeUsuario")
    @Mapping(source = "email", target = "email")
    @Mapping(target = "fotoPerfilUrl", ignore = true)
    UsuarioPerfilResponse toPerfilResponse(UserAccount account);
}
