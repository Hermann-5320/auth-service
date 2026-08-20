package auth_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chauffeur_id", nullable = false)
    private Chauffeur chauffeur;

    @Column(nullable = false)
    private String type;

    private String statut = "EN_ATTENTE";

    private String chemin;

    @Column(name = "raison_rejet")
    private String raisonRejet;

    private LocalDate expiration;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}