package com.zhishu.security;

import com.zhishu.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class AdminJwtUtil {

    private final JwtProperties props;
    private final SecretKey key;

    public AdminJwtUtil(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long adminId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.getExpireHours() * 3600_000L);
        return Jwts.builder()
                .subject(String.valueOf(adminId))
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public Long parseAdminId(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }
}