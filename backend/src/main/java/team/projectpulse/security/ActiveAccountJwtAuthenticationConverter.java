package team.projectpulse.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import team.projectpulse.user.PeerEvaluationUser;
import team.projectpulse.user.UserRepository;

/**
 * Rechecks the account's current enabled state whenever a bearer token is authenticated. This
 * revokes access on the next protected request after an account is deactivated, including when
 * the token was issued before that change.
 */
public class ActiveAccountJwtAuthenticationConverter extends JwtAuthenticationConverter {

    private final UserRepository userRepository;

    public ActiveAccountJwtAuthenticationConverter(UserRepository userRepository) {
        this.userRepository = userRepository;
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("authorities");
        authoritiesConverter.setAuthorityPrefix("");
        setJwtGrantedAuthoritiesConverter(authoritiesConverter);
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Object rawUserId = jwt.getClaims().get("userId");
        if (!(rawUserId instanceof Number userId)) {
            throw new InvalidBearerTokenException("The account for this token is no longer available.");
        }

        this.userRepository.findById(userId.intValue())
                .filter(PeerEvaluationUser::isEnabled)
                .orElseThrow(() -> new InvalidBearerTokenException("The account for this token is disabled or no longer available."));

        return super.convert(jwt);
    }
}
