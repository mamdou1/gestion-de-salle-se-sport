package com.cwa.GestionDeSalleDeSportV2.Jwt;

import com.cwa.GestionDeSalleDeSportV2.Service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtils jwtUtils;

    public JwtFilter(CustomUserDetailsService customUserDetailsService1, JwtUtils jwtUtils1) {
        this.customUserDetailsService = customUserDetailsService1;
        this.jwtUtils = jwtUtils1;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // Ne filtre pas les routes publiques
        if (path.startsWith("/api/auth/")) { // Inclut /api/auth/refresh
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7).trim();
            try {
                username = jwtUtils.extractUsername(jwt);
            } catch (Exception e) {
                System.out.println("Erreur lors de l'extraction du username depuis le token : " + e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            if (jwtUtils.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                // Debug log
                System.out.println("Utilisateur authentifié : " + userDetails.getUsername());
                System.out.println("Rôles : " + userDetails.getAuthorities());
                // Rôle principal (premier rôle)
                if (!userDetails.getAuthorities().isEmpty()) {
                    System.out.println("Rôle principal : " + userDetails.getAuthorities().iterator().next().getAuthority());
                } else {
                    System.out.println("Aucun rôle trouvé");
                }
            } else {
                System.out.println("Token invalide pour l'utilisateur : " + username);
            }
        }

        filterChain.doFilter(request, response);
    }
}
