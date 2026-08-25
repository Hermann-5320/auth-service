package auth_service.controller;

import auth_service.dto.DashboardStatsDTO;
import auth_service.entity.Chauffeur;
import auth_service.entity.Utilisateur;
import auth_service.repository.ChauffeurRepository;
import auth_service.repository.DocumentRepository;
import auth_service.repository.PassagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ChauffeurRepository chauffeurRepository;
    private final PassagerRepository passagerRepository;
    private final DocumentRepository documentRepository;

    @GetMapping("/dashboard-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {

        DashboardStatsDTO stats = new DashboardStatsDTO(
                chauffeurRepository.count(),
                chauffeurRepository.countByStatut(Chauffeur.Statut.ACTIF),
                chauffeurRepository.countByStatut(Chauffeur.Statut.EN_ATTENTE),
                chauffeurRepository.countByStatut(Chauffeur.Statut.BLOQUE),
                chauffeurRepository.countByEnLigneTrue(),
                passagerRepository.count(),
                passagerRepository.countByUtilisateurStatut(Utilisateur.Statut.ACTIF),
                passagerRepository.countByUtilisateurStatut(Utilisateur.Statut.BLOQUE),
                documentRepository.countByStatut("EN_ATTENTE")
        );

        return ResponseEntity.ok(stats);
    }
}