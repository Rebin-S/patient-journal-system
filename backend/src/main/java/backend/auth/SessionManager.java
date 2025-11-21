package backend.auth;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    // token -> userId
    private static final Map<String, Long> TOKENS = new ConcurrentHashMap<>();

    public static String issueToken(long userId) {
        String t = UUID.randomUUID().toString();
        TOKENS.put(t, userId);
        return t;
    }

    public static Long resolveUserId(String token) {
        return token == null ? null : TOKENS.get(token);
    }

    public static void revoke(String token) {
        if (token != null) TOKENS.remove(token);
    }
}
