package com.cwa.GestionDeSalleDeSportV2.Service;


import com.cwa.GestionDeSalleDeSportV2.Entity.Enums.StatutAbonnement;
import com.cwa.GestionDeSalleDeSportV2.Entity.Gym;
import com.cwa.GestionDeSalleDeSportV2.Repository.GymRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GymService {

    private final GymRepository gymRepository;

    public GymService(GymRepository gymRepository) {
        this.gymRepository = gymRepository;
    }

    //  1.  Consulter liste des gym
    public List<Gym> ConsulterGymListe(){
        List<Gym> gym = gymRepository.findAll();
        return gym;
    }

    //  2.  GetById d'un gym pour voir les details
    public Gym getGymById(Long gymId){
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(()->new RuntimeException("Gym introuvable"));
        return gym;
    }

    @Transactional
    public Map<Long, Long> getNombreMembresParGym() {
        Map<Long, Long> nombreMembresParGym = new HashMap<>();
        List<Object[]> results = gymRepository.countMembresParGym();

        for (Object[] result : results) {
            nombreMembresParGym.put((Long) result[0], (Long) result[1]);
        }

        return nombreMembresParGym;
    }

    @Transactional
    public Map<Long, Map<StatutAbonnement, Long>> getNombreMembresParStatutEtGym() {
        Map<Long, Map<StatutAbonnement, Long>> result = new HashMap<>();
        List<Object[]> counts = gymRepository.countMembresParStatutEtGym();

        for (Object[] count : counts) {
            Long gymId = (Long) count[0];
            StatutAbonnement statut = (StatutAbonnement) count[1];
            Long nombre = (Long) count[2];

            result.computeIfAbsent(gymId, k -> new HashMap<>())
                    .put(statut, nombre);}

        // Remplir avec 0 pour les statuts manquants
        for (StatutAbonnement statut : StatutAbonnement.values()) {
            for (Map<StatutAbonnement, Long> statutMap : result.values()) {
                statutMap.putIfAbsent(statut, 0L);
            }
        }

        return result;
    }
}
