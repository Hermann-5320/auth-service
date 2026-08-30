package auth_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Confirmation inscription passager
    public void envoyerConfirmationInscription(String email, String prenom) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Bienvenue sur School Driva !");
        message.setText(
                "Bonjour " + prenom + ",\n\n" +
                        "Votre compte School Driva a été créé avec succès.\n" +
                        "Vous pouvez maintenant réserver des courses sur notre plateforme.\n\n" +
                        "Bonne route !\n" +
                        "L'équipe School Driva"
        );
        mailSender.send(message);
    }

    // Confirmation réception dossier chauffeur
    public void envoyerConfirmationDossier(String email, String prenom) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("School Driva : Dossier reçu  ");
        message.setText(
                "Bonjour " + prenom + ",\n\n" +
                        "Nous avons bien reçu votre dossier de candidature.\n" +
                        "Notre équipe va l'examiner dans les 48 heures.\n" +
                        "Vous recevrez un email de confirmation dès que votre compte sera activé.\n\n" +
                        "Merci de votre confiance.\n" +
                        "L'équipe School Driva"
        );
        mailSender.send(message);
    }

    // Code de réinitialisation mot de passe
    public void envoyerCodeReset(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Réinitialisation de mot de passe School Driva");
        message.setText(
                "Bonjour,\n\n" +
                        "Votre code de réinitialisation de mot de passe est :\n\n" +
                        "   " + code + "\n\n" +
                        "Ce code est valable 15 minutes.\n" +
                        "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
                        "L'équipe School Driva"
        );
        mailSender.send(message);
    }

    // Activation compte chauffeur par admin
    public void envoyerActivationCompte(String email, String prenom, String motDePasse) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Compte activé — School Driva");
        message.setText(
                "Bonjour " + prenom + ",\n\n" +
                        "Votre dossier a été validé. Votre compte est maintenant actif.\n\n" +
                        "Vos identifiants de connexion :\n" +
                        "Email : " + email + "\n" +
                        "Mot de passe temporaire : " + motDePasse + "\n\n" +
                        "Veuillez changer votre mot de passe dès votre première connexion.\n\n" +
                        "Bienvenue dans l'équipe School Driva !\n" +
                        "L'équipe School Driva"
        );
        mailSender.send(message);
    }
}