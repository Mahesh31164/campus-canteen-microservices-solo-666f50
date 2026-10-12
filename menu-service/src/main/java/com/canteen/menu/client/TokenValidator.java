package com.canteen.menu.client;

import com.canteen.common.dto.ValidateResponse;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TokenValidator {

    private final AuthClient authClient;

    public TokenValidator(AuthClient authClient) {
        this.authClient = authClient;
    }

    public ValidateResponse require(String authorization) {
        String token = bearerToken(authorization);
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        try {
            return authClient.validate(token, authorization);
        } catch (FeignException.Unauthorized | FeignException.Forbidden ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } catch (FeignException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable");
        }
    }

    static String bearerToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        if (authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = authorization.substring(7).trim();
            return token.isEmpty() ? null : token;
        }
        return authorization.trim();
    }
}
