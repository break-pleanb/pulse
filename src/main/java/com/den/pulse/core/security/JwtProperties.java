package com.den.pulse.core.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * secret은 application-local.yaml(gitignore 대상)에서만 주입한다. application.yaml에는 두지 않는다.
 */
@ConfigurationProperties(prefix = "pulse.jwt")
public record JwtProperties(
        String secret,
        long accessTokenValiditySeconds,
        long refreshTokenValiditySeconds
) {
}
