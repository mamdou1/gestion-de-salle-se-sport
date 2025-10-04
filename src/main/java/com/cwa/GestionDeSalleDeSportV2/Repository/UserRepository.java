package com.cwa.GestionDeSalleDeSportV2.Repository;

import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.Role;
import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutMembre;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //User findByUsername(String username);

    Optional<User> findByTelephone(String telephone);

    List<User> findByDateRetrait(LocalDate aujourdHui);

    List<User> findByRoleIn(List<Role> admin);

    Optional<User> findByTelephoneOrEmail(String telephone, String email);

    List<User> findByStatutAndRole(StatutMembre statutMembre, Role role);

    List<User> findByGymIn(List<Gym> userGyms);

    Page<User> findByGymInAndRoleIn(List<Gym> gyms, List<Role> roles, Pageable pageable);

    User findByEmail(String email);

    User findByEmailIgnoreCase(String email);

    List<User> findByGymAndFraisInscriptionPayerTrue(Gym gym);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = MEMBRE")
    Long countByMembre();
}
