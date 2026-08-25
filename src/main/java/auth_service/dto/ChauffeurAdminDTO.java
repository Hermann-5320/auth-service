package auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ChauffeurAdminDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String villeNom;
    private String statut;
    private Boolean enLigne;
    private BigDecimal noteMoyenne;
    private Integer nbCourses;
    private LocalDateTime createdAt;
}