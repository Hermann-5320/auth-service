package auth_service.controller;

import auth_service.entity.Document;
import auth_service.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import auth_service.entity.Document;

import java.util.List;

@RestController
@RequestMapping("/api/chauffeur/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentService documentService;

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
}