package analu.whereio.application.service.amigos;

import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.FriendshipStatus;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.amigos.EnviarConviteAmizadeUsecase;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class EnviarConviteAmizadeUsecaseImpl implements EnviarConviteAmizadeUsecase {

    private final FriendshipRepositoryPort friendshipRepositoryPort;
    private final UserAccountRepositoryPort userAccountRepositoryPort;

    @Override
    public Friendship execute(String requesterUserId, String nomeUsuarioDestinatario) {
        MDC.put("operation", "enviarConviteAmizade");
        try {
            String solicitante = requesterUserId != null ? requesterUserId.trim() : "";
//            String nomeUsuarioNorm = NomeUsuarioNormalizer.sanitizePreferencia(nomeUsuarioDestinatario);

            if (solicitante.isBlank()) {
                throw new BusinessException("Usuário solicitante inválido", HttpStatus.BAD_REQUEST);
            }
            if (nomeUsuarioDestinatario.length() < 3) {
                throw new BusinessException("Nome de usuário inválido", HttpStatus.BAD_REQUEST);
            }

            UserAccount destinatarioConta = userAccountRepositoryPort.findByNome(nomeUsuarioDestinatario)
                    .orElseThrow(() -> new BusinessException(
                            "Nenhum usuário encontrado com este nome de usuário.", HttpStatus.NOT_FOUND));

            String destinatario = destinatarioConta.getId();
            if (solicitante.equals(destinatario)) {
                throw new BusinessException("Não é possível convidar a si mesmo", HttpStatus.BAD_REQUEST);
            }

            var existentes = friendshipRepositoryPort.findAllBetweenUsers(solicitante, destinatario);
            for (Friendship f : existentes) {
                if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                    throw new BusinessException("Vocês já são amigos", HttpStatus.CONFLICT);
                }
                if (f.getStatus() == FriendshipStatus.PENDING) {
                    throw new BusinessException("Já existe um convite pendente entre vocês", HttpStatus.CONFLICT);
                }
            }

            Friendship novo = new Friendship();
            novo.setRequesterUserId(solicitante);
            novo.setAddresseeUserId(destinatario);
            novo.setStatus(FriendshipStatus.PENDING);
            novo.setCreatedAt(Instant.now());

            Friendship salvo = friendshipRepositoryPort.save(novo);
            MDC.put("entityId", salvo.getId());
            return salvo;
        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
