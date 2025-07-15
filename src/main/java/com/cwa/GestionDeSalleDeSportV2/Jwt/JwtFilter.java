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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //  Ignore le filtrage pour les routes publiques
        String path = request.getServletPath();

        if (path.equals("/api/auth/inscription") || path.equals("/api/auth/login")){
            filterChain.doFilter(request, response);
            return;
        }

        final String AuthHeaders = request.getHeader("Authorization");

        String jwt = null;
        String username = null;

        if (AuthHeaders != null && AuthHeaders.startsWith("Bearer ")){
            jwt = AuthHeaders.substring(7);
            username = jwtUtils.extractUsername(jwt);
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            if (jwtUtils.validateToken(jwt, userDetails)){
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // ce code ci-dessus va permettre de derterminer avec quel method l'utilisateur a été authentifier, apporter plus de details des logs et d' info telque l'identifiant, l'adresse IP etc etc
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);

    }
}
