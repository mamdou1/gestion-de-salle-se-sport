package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //User findByUsername(String username);

    Optional<User> findByTelephone(String telephone);

    List<User> findByDateRetrait(LocalDate aujourdHui);

    List<User> findByRoleIn(List<Role> admin);
}
