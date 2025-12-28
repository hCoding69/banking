package com.example.banking.Services;

import com.example.banking.Models.Role;
import com.example.banking.Services.dto.AuthResponse;
import com.example.banking.Services.dto.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    private final UserDetailsServiceImpl userDetailsService;

    public JwtService(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Map<String, Object> getClaims(CustomUserDetails user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        // ✅ Les rôles sous forme de String
        claims.put("roles", user.getRoles()
                .stream()
                .map(Role::getName)
                .toList());
        return claims;
    }



    private String buildToken(Map<String, Object> extraClaims, CustomUserDetails user, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(CustomUserDetails user) {
        return buildToken(getClaims(user), user, jwtExpiration);
    }

    public String generateRefreshToken(CustomUserDetails user) {
        return buildToken(getClaims(user), user, refreshExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, CustomUserDetails user) {
        return user.getUsername().equals(extractUsername(token)) && !isTokenExpired(token);
    }

    public Date getExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    public String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(cookieName)) return cookie.getValue();
        }
        return null;
    }

    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request) {
        String refreshToken = extractTokenFromCookie(request, "refreshToken");
        if (refreshToken == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, "Refresh token missing"));

        String email = extractUsername(refreshToken);
        CustomUserDetails user = (CustomUserDetails) userDetailsService.loadUserByUsername(email);

        if (!isTokenValid(refreshToken, user))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, "Invalid refresh token"));

        String newAccessToken = generateToken(user);

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .body(new AuthResponse(null, null, "Access token refreshed"));
    }

    public String generateInternalToken(String serviceName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("internal", true);
        claims.put("service", serviceName);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(serviceName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 1 jour
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateInternalToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSignInKey())
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("internal", Boolean.class) != null && claims.get("internal", Boolean.class);
        } catch (Exception e) {
            return false;
        }
    }

}
