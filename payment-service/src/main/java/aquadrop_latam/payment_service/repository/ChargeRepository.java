package aquadrop_latam.payment_service.repository;

import aquadrop_latam.payment_service.models.Charge;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ChargeRepository extends CrudRepository<Charge, String> {
    Optional<Charge> findByPaymentIntentId(String paymentIntentId);
    List<Charge> findByProvider(String provider);
}
