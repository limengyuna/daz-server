package com.limengyuan.partner.common.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

/**
 * JWT 工具类
 */
public class JwtUtils {

    /**
     * 密钥 (生产环境应该从配置文件读取)
     */
    private static final String SECRET = "partner-finder-jwt-secret-key-2025";

    /**
     * Token 有效期: 7 天
     */
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L;

    /**
     * Token 刷新阈值: 剩余不足 2 天时刷新
     */
    private static final long REFRESH_THRESHOLD = 2 * 24 * 60 * 60 * 1000L;

    /**
     * Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 生成 JWT Token
     *
     * @param userId 用户ID
     * @return JWT Token
     */
    public static String generateToken(Long userId) {
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(SECRET));
    }

    /**
     * 解析 JWT Token，获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID，解析失败返回 null
     */
    public static Long getUserIdFromToken(String token) {
        try {
            DecodedJWT jwt = decodeToken(token);
            return jwt != null ? Long.parseLong(jwt.getSubject()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        return getUserIdFromToken(token) != null;
    }

    /**
     * 检查 Token 是否需要刷新（剩余时间小于 2 天）
     *
     * @param token JWT Token
     * @return 是否需要刷新
     */
    public static boolean shouldRefreshToken(String token) {
        try {
            DecodedJWT jwt = decodeToken(token);
            if (jwt == null) {
                return false;
            }
            Date expiresAt = jwt.getExpiresAt();
            long remainingTime = expiresAt.getTime() - System.currentTimeMillis();
            return remainingTime > 0 && remainingTime < REFRESH_THRESHOLD;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解码 Token
     */
    private static DecodedJWT decodeToken(String token) {
        try {
            // 去掉 Bearer 前缀
            if (token != null && token.startsWith(TOKEN_PREFIX)) {
                token = token.substring(TOKEN_PREFIX.length());
            }
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}
