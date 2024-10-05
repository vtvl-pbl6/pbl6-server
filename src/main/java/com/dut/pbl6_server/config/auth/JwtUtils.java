package com.dut.pbl6_server.config.auth;

import com.dut.pbl6_server.common.constant.CommonConstants;
import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.constant.RedisCacheConstants;
import com.dut.pbl6_server.common.exception.ForbiddenException;
import com.dut.pbl6_server.common.exception.UnauthorizedException;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.dto.respone.CredentialResponse;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.repository.jpa.AccountsRepository;
import com.dut.pbl6_server.repository.jpa.RefreshTokensRepository;
import com.dut.pbl6_server.repository.redis.RedisRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtils {
    @Value("${application.jwt.access-token-expiration-ms}")
    private Long accessTokenExpirationMs;
    @Value("${application.jwt.refresh-token-expiration-ms}")
    private Long refreshTokenExpirationMs;

    private final AccountsRepository accountRepository;
    private final RefreshTokensRepository refreshTokenRepository;
    private final RedisRepository redisRepository;

    public Account getAccountFromToken(String token) {
        Claims jwtClaims = getJwtClaims(token, "access");
        Long accountId = Long.parseLong(jwtClaims.getSubject());

        // Check if the access token is revoked
        if (redisRepository.findAllByHashKeyPrefix(
            RedisCacheConstants.AUTH_KEY,
            RedisCacheConstants.REVOKE_ACCESS_TOKEN_HASH(accountId, false)
        ).contains(token)
        ) {
            throw new UnauthorizedException(ErrorMessageConstants.REVOKED_TOKEN);
        }

        return accountRepository.findById(accountId)
            .orElseThrow(() -> new ForbiddenException(ErrorMessageConstants.FORBIDDEN));
    }

    public CredentialResponse generateToken(Long accountId) {
        return CredentialResponse.builder()
            .accessToken(generateAccessToken(accountId))
            .refreshToken(generateRefreshToken(accountId))
            .build();
    }

    public CredentialResponse refreshToken(String refreshToken, boolean isAdmin) {
        Claims jwtClaims = getJwtClaims(refreshToken, "refresh");
        Long accountId = Long.parseLong(jwtClaims.getSubject());

        // Check if the refresh token is revoked
        if (redisRepository.findAllByHashKeyPrefix(
            RedisCacheConstants.AUTH_KEY,
            RedisCacheConstants.REVOKE_REFRESH_TOKEN_HASH(accountId, false)
        ).contains(refreshToken)
        ) {
            throw new UnauthorizedException(ErrorMessageConstants.REVOKED_REFRESH_TOKEN);
        }

        // Check if the user is an admin
        if (!accountRepository.isAdministrator(accountId) && isAdmin)
            throw new ForbiddenException(ErrorMessageConstants.FORBIDDEN);

        var accountRefreshTokens = refreshTokenRepository.findAllByAccountId(accountId);
        // Check if list refresh token is empty or null
        if (CommonUtils.List.isEmptyOrNull(accountRefreshTokens))
            throw new ForbiddenException(ErrorMessageConstants.REFRESH_TOKEN_NOT_FOUND);

        // Get refresh token from list refresh token
        var accountRefreshToken = accountRefreshTokens.stream()
            .filter(refreshTokenEntity -> refreshToken.equals(refreshTokenEntity.getToken()))
            .findFirst()
            .orElseThrow(() -> new ForbiddenException(ErrorMessageConstants.REFRESH_TOKEN_NOT_FOUND));
        String accessToken = generateAccessToken(accountId);
        return CredentialResponse.builder()
            .accessToken(accessToken)
            .refreshToken(accountRefreshToken.getToken())
            .build();
    }

    private String generateAccessToken(Long accountId) {
        try {
            var privateKey = CommonUtils.RSAKeyLoader.loadPrivateKey(CommonConstants.ACCESS_TOKEN_PRIVATE_KEY_FILE);
            return Jwts.builder()
                .subject(String.valueOf(accountId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(privateKey)
                .compact();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String generateRefreshToken(Long accountId) {
        try {
            var privateKey = CommonUtils.RSAKeyLoader.loadPrivateKey(CommonConstants.REFRESH_TOKEN_PRIVATE_KEY_FILE);
            return Jwts.builder()
                .subject(String.valueOf(accountId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(privateKey)
                .compact();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Claims getJwtClaims(String token, String tokenType) {
        switch (tokenType) {
            case "access":
                try {
                    var publicKey = CommonUtils.RSAKeyLoader.loadPublicKey(CommonConstants.ACCESS_TOKEN_PUBLIC_KEY_FILE);
                    return Jwts.parser()
                        .verifyWith(publicKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                } catch (ExpiredJwtException ex) {
                    throw new UnauthorizedException(ErrorMessageConstants.EXPIRED_TOKEN);
                } catch (Exception ex) {
                    throw new UnauthorizedException(ErrorMessageConstants.INVALID_TOKEN);
                }
            case "refresh":
                try {
                    var publicKey = CommonUtils.RSAKeyLoader.loadPublicKey(CommonConstants.REFRESH_TOKEN_PUBLIC_KEY_FILE);
                    return Jwts.parser()
                        .verifyWith(publicKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                } catch (ExpiredJwtException ex) {
                    throw new UnauthorizedException(ErrorMessageConstants.EXPIRED_REFRESH_TOKEN);
                } catch (Exception ex) {
                    throw new UnauthorizedException(ErrorMessageConstants.INVALID_REFRESH_TOKEN);
                }
            default:
                throw new UnauthorizedException(ErrorMessageConstants.INVALID_TOKEN);
        }
    }
}
