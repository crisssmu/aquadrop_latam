package aquadrop_latam.payment_service.repository;

import aquadrop_latam.payment_service.models.PaymentIntent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PaymentIntentRepository extends CrudRepository<PaymentIntent, String> {
    Optional<PaymentIntent> findByBookingId(String bookingId);
    List<PaymentIntent> findByZone(String zone);
    List<PaymentIntent> findByCustomerId(String customerId);
}
