package auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NouveauMotDePasseDTO {

    @NotBlank(message = "Code obligatoire")
    private String code;

    @NotBlank(message = "Nouveau mot de passe obligatoire")
    @Size(min = 8, message = "Mot de passe minimum 8 caractères")
    private String nouveauMotDePasse;
}