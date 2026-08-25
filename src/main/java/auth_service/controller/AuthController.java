package auth_service.controller;

import auth_service.dto.*;
import auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;


    // POST /api/auth/connexion
    @PostMapping("/connexion")
    public ResponseEntity<TokenDTO> connexion(@Valid @RequestBody ConnexionDTO dto) {
        TokenDTO token = authService.connecter(dto);
        return ResponseEntity.ok(token);
    }

    // POST /api/auth/passager/inscription
    @PostMapping("/passager/inscription")
    public ResponseEntity<String> inscrirePassager(
            @Valid @RequestBody InscriptionPassagerDTO dto) {
        authService.inscrirePassager(dto);
        return ResponseEntity.ok("Compte créé avec succès. Vérifiez votre email.");
    }

    // POST /api/auth/chauffeur/pre-inscription
    @PostMapping("/chauffeur/pre-inscription")
    public ResponseEntity<String> preInscrireChauffeur(
            @Valid @RequestBody PreInscriptionChauffeurDTO dto) {
        authService.preInscrireChauffeur(dto);
        return ResponseEntity.ok("Dossier soumis avec succès. Vous serez contacté sous 48h.");
    }

    // POST /api/auth/mot-de-passe/reset
    @PostMapping("/mot-de-passe/reset")
    public ResponseEntity<String> demanderReset(
            @Valid @RequestBody ResetPasswordDTO dto) {
        authService.demanderResetMotDePasse(dto);
        return ResponseEntity.ok("Code de réinitialisation envoyé sur votre email.");
    }

    // POST /api/auth/mot-de-passe/nouveau
    @PostMapping("/mot-de-passe/nouveau")
    public ResponseEntity<String> nouveauMotDePasse(
            @Valid @RequestBody NouveauMotDePasseDTO dto) {
        authService.reinitialiserMotDePasse(dto);
        return ResponseEntity.ok("Mot de passe modifié avec succès.");
    }
    @GetMapping("/generer-hash")
    public String genererHash(@RequestParam String motDePasse) {
        return passwordEncoder.encode(motDePasse);
    }

    // Valider le dossier d'un chauffeur (Admin uniquement)
    @PutMapping("/admin/chauffeurs/{chauffeurId}/valider")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> validerChauffeur(@PathVariable Long chauffeurId) {
        authService.validerChauffeur(chauffeurId);
        return ResponseEntity.ok("Chauffeur validé et activé avec succès");
    }

    // Bloquer un chauffeur (Admin)
    @PutMapping("/admin/chauffeurs/{chauffeurId}/bloquer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> bloquerChauffeur(@PathVariable Long chauffeurId) {
        authService.bloquerChauffeur(chauffeurId);
        return ResponseEntity.ok("Chauffeur bloqué avec succès");
    }

    // Débloquer un chauffeur (Admin)
    @PutMapping("/admin/chauffeurs/{chauffeurId}/debloquer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> debloquerChauffeur(@PathVariable Long chauffeurId) {
        authService.debloquerChauffeur(chauffeurId);
        return ResponseEntity.ok("Chauffeur débloqué avec succès");
    }

    // Bloquer un passager (Admin)
    @PutMapping("/admin/passagers/{passagerId}/bloquer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> bloquerPassager(@PathVariable Long passagerId) {
        authService.bloquerPassager(passagerId);
        return ResponseEntity.ok("Passager bloqué avec succès");
    }

    // Débloquer un passager (Admin)
    @PutMapping("/admin/passagers/{passagerId}/debloquer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> debloquerPassager(@PathVariable Long passagerId) {
        authService.debloquerPassager(passagerId);
        return ResponseEntity.ok("Passager débloqué avec succès");
    }


}