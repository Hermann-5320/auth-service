package auth_service.service;

import auth_service.entity.Chauffeur;
import auth_service.entity.Document;
import auth_service.repository.ChauffeurRepository;
import auth_service.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ChauffeurRepository chauffeurRepository;

    @Value("${app.upload.dossier}")
    private String dossierUpload;

    private static final List<String> TYPES_AUTORISES = List.of(
            "CNI_RECTO", "CNI_VERSO", "SELFIE_CNI",
            "PERMIS", "PLAN_DOMICILE", "CARTE_GRISE",
            "ASSURANCE", "VISITE_TECHNIQUE",
            "PHOTOS_VEHICULE", "CNI_GARANT", "CONTACTS_GARANT"
    );

    private static final List<String> EXTENSIONS_AUTORISEES = List.of(
            "jpg", "jpeg", "png", "pdf"
    );

    public void uploaderDocument(Long chauffeurId, String type, MultipartFile fichier) {

        // Vérifier que le type est valide
        if (!TYPES_AUTORISES.contains(type.toUpperCase())) {
            throw new RuntimeException("Type de document invalide : " + type);
        }

        // Vérifier que le chauffeur existe
        Chauffeur chauffeur = chauffeurRepository.findById(chauffeurId)
                .orElseThrow(() -> new RuntimeException("Chauffeur introuvable"));

        // Vérifier l'extension du fichier
        String nomFichier = fichier.getOriginalFilename();
        String extension = nomFichier.substring(nomFichier.lastIndexOf('.') + 1).toLowerCase();
        if (!EXTENSIONS_AUTORISEES.contains(extension)) {
            throw new RuntimeException("Format non autorisé. Utilisez JPG, PNG ou PDF.");
        }

        // Vérifier la taille (max 5 Mo)
        if (fichier.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Fichier trop volumineux. Maximum 5 Mo.");
        }

        try {
            // Créer le dossier si inexistant
            Path dossier = Paths.get(dossierUpload, String.valueOf(chauffeurId));
            Files.createDirectories(dossier);

            // Générer un nom unique pour éviter les conflits
            String nomUnique = UUID.randomUUID() + "_" + type.toLowerCase() + "." + extension;
            Path destination = dossier.resolve(nomUnique);

            // Sauvegarder le fichier
            Files.copy(fichier.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            // Vérifier si un document de ce type existe déjà
            Document document = documentRepository
                    .findByChauffeurIdAndType(chauffeurId, type.toUpperCase())
                    .orElse(new Document());

            document.setChauffeur(chauffeur);
            document.setType(type.toUpperCase());
            document.setStatut("EN_ATTENTE");
            document.setChemin(destination.toString());
            document.setUpdatedAt(LocalDateTime.now());

            documentRepository.save(document);

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload du fichier : " + e.getMessage());
        }
    }

    public List<Document> getDocumentsChauffeur(Long chauffeurId) {
        return documentRepository.findByChauffeurId(chauffeurId);
    }
}