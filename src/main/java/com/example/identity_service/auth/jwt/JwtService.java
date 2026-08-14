package com.example.identity_service.auth.jwt;

import com.example.identity_service.user.entity.User;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    @NonFinal
    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    @NonFinal
    @Value("${jwt.access-token-expiration}")
    private long ACCESS_TOKEN_EXPIRATION;

    @NonFinal
    @Value("${jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRATION;

    public String generateAccessToken(User user) throws KeyLengthException, JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        List<String> roles =
                user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        JWTClaimsSet claimsSet =
                new JWTClaimsSet.Builder()
                        .subject(user.getId().toString())
                        .jwtID(UUID.randomUUID().toString())
                        .claim("roles", roles)
                        .claim("type", "access")
                        .issueTime(new Date())
                        .expirationTime(
                                new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                        .build();

        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(new MACSigner(SECRET_KEY.getBytes()));
        return signedJWT.serialize();
    }

    public String generateRefreshToken(User user) throws KeyLengthException, JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet claimsSet =
                new JWTClaimsSet.Builder()
                        .subject(user.getId().toString())
                        .jwtID(UUID.randomUUID().toString())
                        .claim("type", "refresh")
                        .issueTime(new Date())
                        .expirationTime(
                                new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                        .build();

        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(new MACSigner(SECRET_KEY.getBytes()));
        return signedJWT.serialize();
    }
}
