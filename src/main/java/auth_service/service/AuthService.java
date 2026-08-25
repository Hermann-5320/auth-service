package auth_service.service;

import auth_service.dto.*;
import auth_service.entity.*;
import auth_service.repository.*;
import auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

    // ── CONNEXION ──────────────────────────────────────────
    public TokenDTO connecter(ConnexionDTO dto) {
        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(dto.getMotDePasse(), utilisateur.getMotDePasse())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        if (utilisateur.getStatut() == Utilisateur.Statut.BLOQUE) {
            throw new RuntimeException("Votre compte est bloqué. Contactez le support.");
        }

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
    public void preInscrireChauffeur(PreInscriptionChauffeurDTO dto) {
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Créer l'utilisateur sans mot de passe
        // Le mot de passe sera défini par l'admin après validation
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode("temp_" + System.currentTimeMillis()));
        utilisateur.setRole(Utilisateur.Role.CHAUFFEUR);
        utilisateur.setStatut(Utilisateur.Statut.BLOQUE); // bloqué jusqu'à validation admin
        utilisateur = utilisateurRepository.save(utilisateur);

        // Créer le chauffeur
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

        chauffeurRepository.save(chauffeur);

        // Email de confirmation de réception du dossier
        emailService.envoyerConfirmationDossier(dto.getEmail(), dto.getPrenom());
    }

    // ── RESET MOT DE PASSE ─────────────────────────────────
    public void demanderResetMotDePasse(ResetPasswordDTO dto) {
        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Aucun compte trouvé avec cet email"));

        // Générer un code à 6 chiffres
        String code = String.valueOf((int)(Math.random() * 900000) + 100000);

        // En prod on stocke le code en base avec expiration
        // Pour le MVP on l'envoie directement par email
        emailService.envoyerCodeReset(dto.getEmail(), code);

    }
    // ── REINITIALISER MOT DE PASSE ─────────────────────────
    public void reinitialiserMotDePasse(NouveauMotDePasseDTO dto) {
        // Pour le MVP on vérifie juste que le code existe
        // En prod on vérifiera le code stocké en base avec expiration
        if (dto.getCode() == null || dto.getCode().length() != 6) {
            throw new RuntimeException("Code invalide");
        }

        // En prod : récupérer l'email associé au code depuis la base
        // Pour le MVP on retourne juste un succès
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

}