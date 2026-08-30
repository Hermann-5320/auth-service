package auth_service.service;

import auth_service.dto.*;
import auth_service.entity.*;
import auth_service.repository.*;
import auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import auth_service.repository.VehiculeRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PassagerRepository passagerRepository;
    private final ChauffeurRepository chauffeurRepository;
    private final VilleRepository villeRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VehiculeRepository vehiculeRepository;

    // ── CONNEXION ──────────────────────────────────────────
    public TokenDTO connecter(ConnexionDTO dto) {
        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        // Vérifier si le compte est actuellement verrouillé
        if (utilisateur.getVerrouilleJusquA() != null
                && utilisateur.getVerrouilleJusquA().isAfter(java.time.LocalDateTime.now())) {
            long minutesRestantes = java.time.Duration.between(
                    java.time.LocalDateTime.now(),
                    utilisateur.getVerrouilleJusquA()
            ).toMinutes() + 1;
            throw new RuntimeException("Compte temporairement verrouillé. Réessayez dans " + minutesRestantes + " minute(s).");
        }

        if (!passwordEncoder.matches(dto.getMotDePasse(), utilisateur.getMotDePasse())) {
            // Incrémenter le compteur d'échecs
            int tentatives = utilisateur.getTentativesEchouees() == null ? 0 : utilisateur.getTentativesEchouees();
            tentatives++;
            utilisateur.setTentativesEchouees(tentatives);

            if (tentatives >= 5) {
                utilisateur.setVerrouilleJusquA(java.time.LocalDateTime.now().plusMinutes(15));
                utilisateur.setTentativesEchouees(0);
            }

            utilisateurRepository.save(utilisateur);
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        if (utilisateur.getStatut() == Utilisateur.Statut.BLOQUE) {
            throw new RuntimeException("Votre compte est bloqué. Contactez le support.");
        }

        // Connexion réussie — réinitialiser le compteur
        utilisateur.setTentativesEchouees(0);
        utilisateur.setVerrouilleJusquA(null);
        utilisateurRepository.save(utilisateur);

        String token = jwtService.genererToken(
                utilisateur.getEmail(),
                utilisateur.getRole().name(),
                utilisateur.getId()
        );

        return new TokenDTO(token, utilisateur.getRole().name(), utilisateur.getId());
    }

    // ── INSCRIPTION PASSAGER ───────────────────────────────
    @Transactional
    public void inscrirePassager(InscriptionPassagerDTO dto) {
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Créer l'utilisateur
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        utilisateur.setRole(Utilisateur.Role.PASSAGER);
        utilisateur = utilisateurRepository.save(utilisateur);

        // Créer le passager
        Passager passager = new Passager();
        passager.setUtilisateur(utilisateur);
        passager.setNom(dto.getNom().toUpperCase());
        passager.setPrenom(dto.getPrenom());
        passager.setTelephone(dto.getTelephone());

        if (dto.getVilleId() != null) {
            villeRepository.findById(dto.getVilleId())
                    .ifPresent(passager::setVille);
        }

        passagerRepository.save(passager);

        // Envoyer email de confirmation
        emailService.envoyerConfirmationInscription(dto.getEmail(), dto.getPrenom());
    }

    // ── PRE-INSCRIPTION CHAUFFEUR ──────────────────────────
    @Transactional
    public Long preInscrireChauffeur(PreInscriptionChauffeurDTO dto) {
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode("temp_" + System.currentTimeMillis()));
        utilisateur.setRole(Utilisateur.Role.CHAUFFEUR);
        utilisateur.setStatut(Utilisateur.Statut.BLOQUE);
        utilisateur = utilisateurRepository.save(utilisateur);

        Chauffeur chauffeur = new Chauffeur();
        chauffeur.setUtilisateur(utilisateur);
        chauffeur.setNom(dto.getNom().toUpperCase());
        chauffeur.setPrenom(dto.getPrenom());
        chauffeur.setTelephone(dto.getTelephone());
        chauffeur.setStatut(Chauffeur.Statut.EN_ATTENTE);

        if (dto.getVilleId() != null) {
            villeRepository.findById(dto.getVilleId())
                    .ifPresent(chauffeur::setVille);
        }

        chauffeur = chauffeurRepository.save(chauffeur);

        Vehicule vehicule = new Vehicule();
        vehicule.setChauffeur(chauffeur);
        vehicule.setType(dto.getTypeVehicule().toUpperCase());
        vehicule.setMarque(dto.getMarque());
        vehicule.setModele(dto.getModele());
        vehicule.setCouleur(dto.getCouleur());
        vehicule.setAnnee(dto.getAnnee());
        vehicule.setImmatriculation(dto.getImmatriculation());
        vehiculeRepository.save(vehicule);

        emailService.envoyerConfirmationDossier(dto.getEmail(), dto.getPrenom());

        return chauffeur.getId();
    }

    // ── DEMANDER UN RESET DE MOT DE PASSE ─────────────────
    @Transactional
    public void demanderResetMotDePasse(ResetPasswordDTO dto) {
        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Aucun compte trouvé avec cet email"));

        String code = String.valueOf((int)(Math.random() * 900000) + 100000);

        utilisateur.setCodeReset(code);
        utilisateur.setCodeResetExpire(java.time.LocalDateTime.now().plusMinutes(15));
        utilisateurRepository.save(utilisateur);

        emailService.envoyerCodeReset(dto.getEmail(), code);
    }

    // ── REINITIALISER LE MOT DE PASSE ─────────────────────
    @Transactional
    public void reinitialiserMotDePasse(NouveauMotDePasseDTO dto) {
        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Aucun compte trouvé avec cet email"));

        if (utilisateur.getCodeReset() == null || !utilisateur.getCodeReset().equals(dto.getCode())) {
            throw new RuntimeException("Code invalide");
        }

        if (utilisateur.getCodeResetExpire() == null
                || utilisateur.getCodeResetExpire().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Ce code a expiré. Veuillez en demander un nouveau.");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(dto.getNouveauMotDePasse()));
        utilisateur.setCodeReset(null);
        utilisateur.setCodeResetExpire(null);
        utilisateurRepository.save(utilisateur);
    }
    // ── VALIDER UN CHAUFFEUR (ADMIN) ───────────────────────
    @Transactional
    public void validerChauffeur(Long chauffeurId) {
        Chauffeur chauffeur = chauffeurRepository.findById(chauffeurId)
                .orElseThrow(() -> new RuntimeException("Chauffeur introuvable"));

        Utilisateur utilisateur = chauffeur.getUtilisateur();

        // Générer un mot de passe temporaire lisible
        String motDePasseTemp = genererMotDePasseTemporaire();

        utilisateur.setMotDePasse(passwordEncoder.encode(motDePasseTemp));
        utilisateur.setStatut(Utilisateur.Statut.ACTIF);
        utilisateurRepository.save(utilisateur);

        chauffeur.setStatut(Chauffeur.Statut.ACTIF);
        chauffeurRepository.save(chauffeur);

        // Envoyer les identifiants par email
        emailService.envoyerActivationCompte(
                utilisateur.getEmail(),
                chauffeur.getPrenom(),
                motDePasseTemp
        );
    }

    // Génère un mot de passe temporaire lisible (ex: SD-4829)
    private String genererMotDePasseTemporaire() {
        int code = (int)(Math.random() * 9000) + 1000;
        return "SD-" + code;
    }
    // ── BLOQUER / DEBLOQUER CHAUFFEUR (ADMIN) ──────────────
    @Transactional
    public void bloquerChauffeur(Long chauffeurId) {
        Chauffeur chauffeur = chauffeurRepository.findById(chauffeurId)
                .orElseThrow(() -> new RuntimeException("Chauffeur introuvable"));

        Utilisateur utilisateur = chauffeur.getUtilisateur();
        utilisateur.setStatut(Utilisateur.Statut.BLOQUE);
        utilisateurRepository.save(utilisateur);

        chauffeur.setStatut(Chauffeur.Statut.BLOQUE);
        chauffeur.setEnLigne(false);
        chauffeurRepository.save(chauffeur);
    }

    @Transactional
    public void debloquerChauffeur(Long chauffeurId) {
        Chauffeur chauffeur = chauffeurRepository.findById(chauffeurId)
                .orElseThrow(() -> new RuntimeException("Chauffeur introuvable"));

        Utilisateur utilisateur = chauffeur.getUtilisateur();
        utilisateur.setStatut(Utilisateur.Statut.ACTIF);
        utilisateurRepository.save(utilisateur);

        chauffeur.setStatut(Chauffeur.Statut.ACTIF);
        chauffeurRepository.save(chauffeur);
    }

    // ── BLOQUER / DEBLOQUER PASSAGER (ADMIN) ───────────────
    @Transactional
    public void bloquerPassager(Long passagerId) {
        Passager passager = passagerRepository.findById(passagerId)
                .orElseThrow(() -> new RuntimeException("Passager introuvable"));

        Utilisateur utilisateur = passager.getUtilisateur();
        utilisateur.setStatut(Utilisateur.Statut.BLOQUE);
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void debloquerPassager(Long passagerId) {
        Passager passager = passagerRepository.findById(passagerId)
                .orElseThrow(() -> new RuntimeException("Passager introuvable"));

        Utilisateur utilisateur = passager.getUtilisateur();
        utilisateur.setStatut(Utilisateur.Statut.ACTIF);
        utilisateurRepository.save(utilisateur);
    }

}