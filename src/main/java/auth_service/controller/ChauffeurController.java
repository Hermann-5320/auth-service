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
}