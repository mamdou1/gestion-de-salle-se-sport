package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.DTO.ConnexionDTO;
import com.cwa.GestionDeSalleDeSportV2.DTO.InscriptionDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import com.cwa.GestionDeSalleDeSportV2.Jwt.JwtUtils;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import com.cwa.GestionDeSalleDeSportV2.Repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, GymRepository gymRepository, JwtUtils jwtUtils, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.gymRepository = gymRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public String inscriptionAdmin(InscriptionDTO dto) {

        Gym gym = new Gym();
        gym.setNom(dto.getNomGym());
        gym.setAdresse(dto.getAdresseGym());
        gym.setEmail(dto.getEmailGym());
        gym.setTelephone(dto.getTelephoneGym());

        gymRepository.save(gym);

        // 2. Créer l’utilisateur admin lié à ce gym
        User admin = new User();
        admin.setNom(dto.getNomAdmin());
        admin.setPrenom(dto.getPrenomAdmin());
        admin.setAdresse(dto.getAdresseAdmin());
        admin.setEmail(dto.getEmailAdmin());
        admin.setRole(Role.ADMIN);
        admin.setGenre(dto.getGenre());
        admin.setDate_de_naissance(dto.getDate_de_naissance());
        admin.setDate_creation(LocalDateTime.now());
      //  admin.setOnline(false);
        admin.setGym(gym);

        admin.setTelephone(dto.getTelephoneAdmin());
        admin.setPassword(passwordEncoder.encode(dto.getPasswordAdmin()));

        userRepository.save(admin);

        return jwtUtils.generateToken(admin);
    }

    public String connexion(ConnexionDTO dto){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getTelephone(), dto.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return jwtUtils.generateToken(userDetails);
    }
}

