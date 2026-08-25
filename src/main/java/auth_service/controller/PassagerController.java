package auth_service.controller;

import auth_service.entity.Passager;
import auth_service.repository.PassagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import auth_service.dto.PassagerAdminDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/passagers")
@RequiredArgsConstructor
public class PassagerController {

    private final PassagerRepository passagerRepository;

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
}