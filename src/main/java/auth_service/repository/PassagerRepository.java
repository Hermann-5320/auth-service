package auth_service.repository;

import auth_service.entity.Passager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PassagerRepository extends JpaRepository<Passager, Long> {
    Optional<Passager> findByUtilisateurId(Long utilisateurId);
}