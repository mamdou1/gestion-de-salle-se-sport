package com.cwa.GestionDeSalleDeSportV2.Jwt;

import com.cwa.GestionDeSalleDeSportV2.Service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtils jwtUtils;

    public JwtFilter(CustomUserDetailsService customUserDetailsService, JwtUtils jwtUtils) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        logger.info("Requête reçue pour le chemin : {}", path);

        // Ne filtre pas les routes publiques
        if (path.startsWith("/api/auth/")) {
            logger.info("Route publique, aucun filtrage JWT : {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        logger.info("Header Authorization : {}", authHeader);

        String jwt = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7).trim();
            logger.info("Token JWT extrait : {}", jwt);
            try {
                username = jwtUtils.extractUsername(jwt);
                logger.info("Username extrait du token : {}", username);
            } catch (Exception e) {
                logger.error("Erreur lors de l'extraction du username depuis le token : {}", e.getMessage());
            }
        } else {
            logger.warn("Aucun token JWT valide trouvé dans le header Authorization");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
            logger.info("UserDetails chargé pour : {}", username);

            if (jwtUtils.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                logger.info("Utilisateur authentifié : {}. Rôles : {}", userDetails.getUsername(), userDetails.getAuthorities());
            } else {
                logger.warn("Token invalide pour l'utilisateur : {}", username);
            }
        } else {
            logger.warn("Aucune authentification effectuée : username={} ou authentication déjà présente", username);
        }

        filterChain.doFilter(request, response);
    }
}