package aquadrop_latam.payment_service.repository;

import aquadrop_latam.payment_service.models.Subsidy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface SubsidyRepository extends CrudRepository<Subsidy, String> {
    Optional<Subsidy> findByZone(String zone);
    List<Subsidy> findByActive(Boolean active);
    List<Subsidy> findByRule(String rule);
}
