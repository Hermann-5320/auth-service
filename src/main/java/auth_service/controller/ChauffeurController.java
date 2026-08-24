package auth_service.controller;

import auth_service.entity.Chauffeur;
import auth_service.repository.ChauffeurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chauffeurs")
@RequiredArgsConstructor
public class ChauffeurController {

    private final ChauffeurRepository chauffeurRepository;

    @GetMapping("/by-utilisateur/{utilisateurId}")
    public ResponseEntity<Long> getChauffeurId(@PathVariable Long utilisateurId) {
        Chauffeur chauffeur = chauffeurRepository.findByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Chauffeur introuvable"));
        return ResponseEntity.ok(chauffeur.getId());
    }
}