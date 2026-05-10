package analu.whereio.application.util;

import java.util.Locale;

public final class NomeUsuarioNormalizer {

    private NomeUsuarioNormalizer() {
    }

    /** Mantém apenas [a-z0-9._-], minúsculas; trunca em 30 caracteres */
    public static String sanitizePreferencia(String raw) {
        if (raw == null) {
            return "";
        }
        String lower = raw.trim().toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lower.length() && sb.length() < 30; i++) {
            char c = lower.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '.' || c == '_' || c == '-') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String baseApartirDoEmail(String normalizedEmail) {
        int at = normalizedEmail.indexOf('@');
        String local = at > 0 ? normalizedEmail.substring(0, at) : normalizedEmail;
        return sanitizePreferencia(local);
    }
}
