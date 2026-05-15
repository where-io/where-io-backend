package analu.whereio.application.ports.out;

import analu.whereio.application.model.ParsedAccessToken;
import analu.whereio.application.model.UserAccount;

public interface JwtAuthenticationPort {

    String generateAccessToken(UserAccount user);

    ParsedAccessToken parseAccessToken(String token);
}
