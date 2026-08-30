package auth_service.controller;

import auth_service.entity.Passager;
import auth_service.repository.PassagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import auth_service.dto.PassagerAdminDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

import auth_service.dto.PassagerProfilDTO;
import auth_service.entity.Utilisateur;
import auth_service.repository.UtilisateurRepository;
import auth_service.security.JwtService;


@RestController
@RequestMapping("/api/passagers")
@RequiredArgsConstructor
public class PassagerController {

    private final PassagerRepository passagerRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;

    @GetMapping("/by-utilisateur/{utilisateurId}")
    public ResponseEntity<Long> getPassagerId(@PathVariable Long utilisateurId) {
        Passager passager = passagerRepository.findByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Passager introuvable"));
        return ResponseEntity.ok(passager.getId());
    }
    @GetMapping("/admin/liste")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PassagerAdminDTO>> listePassagersAdmin(
            @RequestParam(required = false) String statut) {

        List<Passager> passagers = passagerRepository.findAll();

        List<PassagerAdminDTO> resultat = passagers.stream()
                .filter(p -> statut == null || p.getUtilisateur().getStatut().name().equals(statut))
                .map(p -> new PassagerAdminDTO(
                        p.getId(),
                        p.getNom(),
                        p.getPrenom(),
                        p.getUtilisateur().getEmail(),
                        p.getTelephone(),
                        p.getVille() != null ? p.getVille().getNom() : null,
                        p.getUtilisateur().getStatut().name(),
                        p.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(resultat);
    }
    @GetMapping("/mon-profil")
    public ResponseEntity<PassagerProfilDTO> getMonProfil(
            @RequestHeader("Authorization") String token) {

        String jwt = token.substring(7);
        String email = jwtService.extraireEmail(jwt);

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Passager passager = passagerRepository.findByUtilisateurId(utilisateur.getId())
                .orElseThrow(() -> new RuntimeException("Passager introuvable"));

        PassagerProfilDTO profil = new PassagerProfilDTO(
                passager.getId(),
                passager.getNom(),
                passager.getPrenom(),
                utilisateur.getEmail(),
                passager.getTelephone(),
                passager.getVille() != null ? passager.getVille().getNom() : null,
                utilisateur.getStatut().name(),
                passager.getCreatedAt()
        );

        return ResponseEntity.ok(profil);
    }
}