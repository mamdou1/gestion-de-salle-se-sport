package com.cwa.GestionDeSalleDeSportV2.Configuration;

import com.cwa.GestionDeSalleDeSportV2.Jwt.JwtFilter;
import com.cwa.GestionDeSalleDeSportV2.Jwt.JwtUtils;
import com.cwa.GestionDeSalleDeSportV2.Service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtils jwtUtils;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtUtils jwtUtils) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtils = jwtUtils;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(
                                        "/api/auth/**",
                                        "/images/**",
                                        "/uploads/**",
                                        "/api/demandeInscriptions/inscriptin/en-ligne",
                                        "/error",
                                        "/swagger-ui/**",
                                        "/api-docs/**",
                                        "/v3/api-docs/**",

                                        // AJOUTÉ : tous les endpoints produits publics
                                        "/api/produits/**",
                                        "/api/abonnements/**",
                                        "/api/forfaits/**",
                                        "/api/cours-collectifs/**",
                                        "/api/tarifs/**",
                                        "/api/horaires/**",
                                        "/api/coaches/**",
                                        "/api/paniers/envoie_panier",
                                        "/api/paniers/envoie_panier/actif/**"
                                ).permitAll()
                                .requestMatchers("/api/demandeInscriptions/valider/**").hasAnyRole("ADMIN", "GERANT", "RECEPTIONNISTE")
                                .requestMatchers("/api/demandeInscriptions/rejeter/**").hasAnyRole("ADMIN", "GERANT", "RECEPTIONNISTE")
                                .requestMatchers("/api/demandeInscriptions/attente").hasAnyRole("ADMIN", "GERANT", "RECEPTIONNISTE")
                                .requestMatchers("/api/demandeInscriptions/gym/**").hasAnyRole("ADMIN", "GERANT", "RECEPTIONNISTE")
                                .requestMatchers("/api/users/changer").authenticated()
                                .requestMatchers("/api/services/app/**").authenticated()
                                .requestMatchers("/api/familles/**").authenticated()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtFilter(customUserDetailsService, jwtUtils), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        return new DefaultMethodSecurityExpressionHandler();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }
}