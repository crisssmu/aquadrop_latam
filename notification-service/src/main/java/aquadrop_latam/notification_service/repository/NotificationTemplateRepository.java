package aquadrop_latam.notification_service.repository;

import aquadrop_latam.notification_service.models.NotificationTemplate;
import aquadrop_latam.notification_service.models.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, String> {
    Optional<NotificationTemplate> findByCode(String code);
    
    List<NotificationTemplate> findByChannel(Channel channel);
    
    List<NotificationTemplate> findByActive(boolean active);
    
    List<NotificationTemplate> findByChannelAndActive(Channel channel, boolean active);
}
