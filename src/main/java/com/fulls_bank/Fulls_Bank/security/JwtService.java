package com.fulls_bank.Fulls_Bank.security;

import com.fulls_bank.Fulls_Bank.entities.AppUser;
import com.fulls_bank.Fulls_Bank.dto.TokenResponseDTO;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtService {

    public static final String ISSUER = "fulls-bank";
    public static final String ROLE_CLAIM = "role";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    public JwtService(JwtEncoder jwtEncoder, JwtProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    public TokenResponseDTO generateToken(AppUser user) {
        Instant now = Instant.now();
        Duration lifetime = Duration.ofMinutes(properties.expirationMinutes());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(user.getEmail())
                .issuedAt(now)
                .expiresAt(now.plus(lifetime))
                .claim(ROLE_CLAIM, user.getRole().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponseDTO(token, "Bearer", lifetime.toSeconds());
    }
}
