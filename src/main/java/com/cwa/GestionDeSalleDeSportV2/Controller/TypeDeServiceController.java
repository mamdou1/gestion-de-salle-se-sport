package com.cwa.GestionDeSalleDeSportV2.Controller;

import com.cwa.GestionDeSalleDeSportV2.DTO.TypeDeServiceDTO;
import com.cwa.GestionDeSalleDeSportV2.Entity.TypeDeService;
import com.cwa.GestionDeSalleDeSportV2.Service.TypeDeServiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/services")
public class TypeDeServiceController {

    private final TypeDeServiceService typeDeServiceService;

    public TypeDeServiceController(TypeDeServiceService typeDeServiceService) {
        this.typeDeServiceService = typeDeServiceService;
    }

    //  1.  Créer les type de service
    @PostMapping
    public ResponseEntity<TypeDeService> createTypeDeService(@RequestBody TypeDeServiceDTO dto) throws AccessDeniedException {
        TypeDeService service = typeDeServiceService.createTypeDeService(dto);
        return new ResponseEntity<>(service, HttpStatus.CREATED);
    }

    //  2.  Mettre à jour un type de service
    @PutMapping("/{id}")
    public ResponseEntity<TypeDeService> updateTypeDeService(@RequestBody TypeDeServiceDTO dto, @PathVariable Long id) throws AccessDeniedException {
        TypeDeService service = typeDeServiceService.updateTypeDeService(id, dto);
        return ResponseEntity.ok(service);
    }

    //  3.  Supprimer un type de service
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTypeDeService(@PathVariable Long id) throws AccessDeniedException {
        typeDeServiceService.deleteTypeDeService(id);
        return ResponseEntity.noContent().build();
    }

    //  4.  getById un type de service
    @GetMapping("/{id}")
    public ResponseEntity<TypeDeService> getTypeDeServiceById(@PathVariable Long id) throws AccessDeniedException {
        TypeDeService service = typeDeServiceService.getTypeDeServiceById(id);
        return ResponseEntity.ok(service);
    }

    //   5. Conslter tout les types de services
    @GetMapping
    public ResponseEntity<List<TypeDeService>> getAllTypeDeService() throws AccessDeniedException {
        List<TypeDeService> services = typeDeServiceService.getAllTypeDeService();
        return ResponseEntity.ok(services);
    }
}