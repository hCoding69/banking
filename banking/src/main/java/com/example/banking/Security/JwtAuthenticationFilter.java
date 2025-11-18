package com.example.banking.Security;

import com.example.banking.Services.JwtService;
import com.example.banking.Services.UserDetailsServiceImpl;
import com.example.banking.Services.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = null;

        // 1️⃣ Récupérer le JWT depuis le cookie "accessToken"
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                System.out.println("Cookie reçu: " + cookie.getName() + "=" + cookie.getValue());
                if ("accessToken".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                }
            }
        }

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2️⃣ Extraire l'email de l'utilisateur depuis le token
        String userEmail;
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            System.out.println("[WARN] Token invalide ou corrompu: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 3️⃣ Authentifier si nécessaire
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            if (!(userDetails instanceof CustomUserDetails customUser)) {
                System.out.println("[WARN] userDetails n'est pas CustomUserDetails : " + userDetails.getClass());
                filterChain.doFilter(request, response);
                return;
            }

            if (jwtService.isTokenValid(jwt, customUser)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                customUser,
                                null,
                                customUser.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("Authentification OK pour: " + userEmail);
            } else {
                System.out.println("[WARN] Token expiré ou invalide pour: " + userEmail);
            }
        }

        filterChain.doFilter(request, response);
    }
}
