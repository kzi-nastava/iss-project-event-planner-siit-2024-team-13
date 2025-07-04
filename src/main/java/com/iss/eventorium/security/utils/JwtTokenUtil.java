package com.iss.eventorium.security.utils;

import com.iss.eventorium.user.models.Role;
import com.iss.eventorium.user.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenUtil {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${jwt_secret}")
    public String secret;

    @Getter
    @Value("${jwt_expires}")
    private Long expiresIn;

    @Value("${auth_header}")
    private String authHeader;

    private static final String AUDIENCE_WEB = "web";
    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    public String generateToken(User user) {
        return Jwts.builder()
                .setIssuer(appName)
                .setSubject(user.getEmail())
                .setAudience(generateAudience())
                .setIssuedAt(new Date())
                .claim("userId", user.getId())
                .claim("roles", user.getRoles().stream()
                        .map(Role::getAuthority)
                        .toList())
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, secret.getBytes()).compact();
    }

    private String generateAudience() {
        return AUDIENCE_WEB;
    }

    private Date generateExpirationDate() {
        return new Date(new Date().getTime() + expiresIn);
    }

    public String getToken(HttpServletRequest request) {
        String header = getAuthHeaderFromHeader(request);
        if (header != null && header.startsWith("Bearer "))
            return header.substring(7);

        return null;
    }

    public String getUsernameFromToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        return (claims != null) ? claims.getSubject() : null;

    }

    public Date getIssuedAtDateFromToken(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            return (claims != null) ? claims.getIssuedAt() : null;
        } catch (ExpiredJwtException ex) {
            throw ex;
        } catch (Exception e) {
            return null;
        }
    }

    private Claims getAllClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            throw ex;
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        User user = (User) userDetails;
        final String username = getUsernameFromToken(token);
        final Date created = getIssuedAtDateFromToken(token);

        if (user.isDeactivated())
            throw new DisabledException("User is deactivated");


        return (username != null
                && username.equals(userDetails.getUsername())
                && !isCreatedBeforeLastPasswordReset(created, user.getLastPasswordReset())
                && !user.isDeactivated());
    }

    private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
        return (lastPasswordReset != null && created.before(lastPasswordReset));
    }

    public String getAuthHeaderFromHeader(HttpServletRequest request) {
        return request.getHeader(authHeader);
    }
}
