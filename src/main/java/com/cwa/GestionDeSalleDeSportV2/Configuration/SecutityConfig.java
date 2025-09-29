package com.cwa.GestionDeSalleDeSportV2.Configuration;


import com.cwa.GestionDeSalleDeSportV2.Jwt.JwtFilter;
import com.cwa.GestionDeSalleDeSportV2.Jwt.JwtUtils;
import com.cwa.GestionDeSalleDeSportV2.Service.CustomUserDetailsService;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecutityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtils jwtUtils;

    public SecutityConfig(CustomUserDetailsService customUserDetailsService, JwtUtils jwtUtils) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtils = jwtUtils;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(Session ->
                        Session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(
                                        "/api/auth/**",
                                        "/api/demandeInscriptions/inscriptin/en-ligne",
                                        "/error",
                                        "/swagger-ui/**",
                                        "/api-docs/**",
                                        "/v3/api-docs/**"
                                ).permitAll()
                                .requestMatchers("/api/users/changer").authenticated() // 🔥 Protéger cette route
                                .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtFilter(customUserDetailsService, jwtUtils), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
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

//    @Bean
//    public JwtDecoder jwtDecoder() {
//        // Remplacez par l'URL de votre serveur d'authentification ou utilisez un decoder mock pour tests
//        return NimbusJwtDecoder.withJwkSetUri("https://your-auth-server/.well-known/jwks.json").build();
//    }
}
