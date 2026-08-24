package auth_service.controller;

import auth_service.entity.Passager;
import auth_service.repository.PassagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}