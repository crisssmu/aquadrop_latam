package aquadrop_latam.payment_service.repository;

import aquadrop_latam.payment_service.models.Refund;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RefundRepository extends CrudRepository<Refund, String> {
    List<Refund> findByChargeId(String chargeId);
    List<Refund> findByStatus(String status);
}
