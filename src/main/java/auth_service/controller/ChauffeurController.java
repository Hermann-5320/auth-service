package auth_service.controller;

import auth_service.entity.Chauffeur;
import auth_service.entity.Utilisateur;
import auth_service.repository.ChauffeurRepository;
import auth_service.repository.UtilisateurRepository;
import auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import auth_service.dto.ChauffeurDisponibleDTO;
import auth_service.dto.ChauffeurAdminDTO;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/chauffeurs")
@RequiredArgsConstructor
public class ChauffeurController {

    private final ChauffeurRepository chauffeurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;

    @GetMapping("/by-utilisateur/{utilisateurId}")
    public ResponseEntity<Long> getChauffeurId(@PathVariable Long utilisateurId) {
        Chauffeur chauffeur = chauffeurRepository.findByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Chauffeur introuvable"));
        return ResponseEntity.ok(chauffeur.getId());
    }

    @GetMapping("/en-ligne")
    public ResponseEntity<List<ChauffeurDisponibleDTO>> getChauffeursEnLigne(@RequestParam Long villeId) {
        List<Chauffeur> chauffeurs = chauffeurRepository
                .findByVille_IdAndEnLigneTrueAndStatut(villeId, Chauffeur.Statut.ACTIF);

        List<ChauffeurDisponibleDTO> resultat = chauffeurs.stream()
                .map(c -> new ChauffeurDisponibleDTO(
                        c.getId(),
                        c.getNom(),
                        c.getPrenom(),
                        c.getTelephone(),
                        c.getNoteMoyenne(),
                        c.getVille().getNom()
                ))
                .toList();

        return ResponseEntity.ok(resultat);
    }
    @PutMapping("/statut-ligne")
    public ResponseEntity<String> basculerStatutLigne(
            @RequestParam Boolean enLigne,
            @RequestHeader("Authorization") String token) {

        String jwt = token.substring(7);
        String email = jwtService.extraireEmail(jwt);

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Chauffeur chauffeur = chauffeurRepository.findByUtilisateurId(utilisateur.getId())
                .orElseThrow(() -> new RuntimeException("Chauffeur introuvable"));

        chauffeur.setEnLigne(enLigne);
        chauffeurRepository.save(chauffeur);

        return ResponseEntity.ok(enLigne ? "Vous êtes en ligne" : "Vous êtes hors ligne");
    }

    // Liste complète des chauffeurs (Admin) — avec filtre optionnel par statut
    @GetMapping("/admin/liste")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChauffeurAdminDTO>> listeChauffeursAdmin(
            @RequestParam(required = false) String statut) {

        List<Chauffeur> chauffeurs = (statut != null)
                ? chauffeurRepository.findByStatut(Chauffeur.Statut.valueOf(statut))
                : chauffeurRepository.findAll();

        List<ChauffeurAdminDTO> resultat = chauffeurs.stream()
                .map(c -> new ChauffeurAdminDTO(
                        c.getId(),
                        c.getNom(),
                        c.getPrenom(),
                        c.getUtilisateur().getEmail(),
                        c.getTelephone(),
                        c.getVille() != null ? c.getVille().getNom() : null,
                        c.getStatut().name(),
                        c.getEnLigne(),
                        c.getNoteMoyenne(),
                        c.getNbCourses(),
                        c.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(resultat);
    }
    @PutMapping("/{chauffeurId}/stats")
    public ResponseEntity<String> mettreAJourStats(
            @PathVariable Long chauffeurId,
            @RequestBody Map<String, Object> body) {

        Chauffeur chauffeur = chauffeurRepository.findById(chauffeurId)
                .orElseThrow(() -> new RuntimeException("Chauffeur introuvable"));

        if (body.containsKey("noteMoyenne")) {
            chauffeur.setNoteMoyenne(BigDecimal.valueOf(((Number) body.get("noteMoyenne")).doubleValue()));
        }

        if (body.containsKey("incrementerCourses") && (Boolean) body.get("incrementerCourses")) {
            chauffeur.setNbCourses(chauffeur.getNbCourses() + 1);
        }

        chauffeurRepository.save(chauffeur);
        return ResponseEntity.ok("Stats mises à jour");
    }
}