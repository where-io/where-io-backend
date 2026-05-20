package analu.whereio.application.model;

import java.util.List;

public record ParsedAccessToken(String userId, String email, List<String> roles) {
}
