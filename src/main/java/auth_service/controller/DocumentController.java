package auth_service.controller;

import auth_service.entity.Document;
import auth_service.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import auth_service.dto.DocumentAdminDTO;
import auth_service.dto.RejetDocumentDTO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import auth_service.repository.DocumentRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;

@RestController
@RequestMapping("/api/chauffeur/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentRepository documentRepository;

    // POST /api/chauffeur/documents/upload
    @PostMapping("/upload")
    public ResponseEntity<String> uploaderDocument(
            @RequestParam("chauffeurId") Long chauffeurId,
            @RequestParam("type") String type,
            @RequestParam("fichier") MultipartFile fichier) {

        if (fichier.isEmpty()) {
            return ResponseEntity.badRequest().body("Fichier vide");
        }

        documentService.uploaderDocument(chauffeurId, type, fichier);
        return ResponseEntity.ok("Document " + type + " soumis avec succès");
    }

    // GET /api/chauffeur/documents/{chauffeurId}
    @GetMapping("/{chauffeurId}")
    public ResponseEntity<List<Document>> getDocuments(@PathVariable Long chauffeurId) {
        return ResponseEntity.ok(documentService.getDocumentsChauffeur(chauffeurId));
    }
    // Voir tous les documents en attente (Admin)
    @GetMapping("/admin/en-attente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocumentAdminDTO>> documentsEnAttente() {
        List<Document> documents = documentRepository.findByStatut("EN_ATTENTE");

        List<DocumentAdminDTO> resultat = documents.stream()
                .map(d -> new DocumentAdminDTO(
                        d.getId(),
                        d.getChauffeur().getId(),
                        d.getChauffeur().getPrenom() + " " + d.getChauffeur().getNom(),
                        d.getType(),
                        d.getStatut(),
                        d.getChemin(),
                        d.getRaisonRejet(),
                        d.getUpdatedAt()
                ))
                .toList();

        return ResponseEntity.ok(resultat);
    }

    // Valider un document (Admin)
    @PutMapping("/admin/{documentId}/valider")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> validerDocument(@PathVariable Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document introuvable"));

        document.setStatut("VALIDE");
        document.setRaisonRejet(null);
        documentRepository.save(document);

        return ResponseEntity.ok("Document validé avec succès");
    }

    // Rejeter un document (Admin)
    @PutMapping("/admin/{documentId}/rejeter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> rejeterDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody RejetDocumentDTO dto) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document introuvable"));

        document.setStatut("REJETE");
        document.setRaisonRejet(dto.getRaison());
        documentRepository.save(document);

        return ResponseEntity.ok("Document rejeté");
    }
    // Recuperer un fichier
    @GetMapping("/admin/{documentId}/fichier")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> getFichierDocument(@PathVariable Long documentId) throws IOException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document introuvable"));

        Path chemin = Paths.get(document.getChemin());
        Resource resource = new UrlResource(chemin.toUri());

        if (!resource.exists()) {
            throw new RuntimeException("Fichier introuvable sur le serveur");
        }

        String contentType = Files.probeContentType(chemin);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .body(resource);
    }
}