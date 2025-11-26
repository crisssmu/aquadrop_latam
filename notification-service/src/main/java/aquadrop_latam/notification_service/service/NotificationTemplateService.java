package aquadrop_latam.notification_service.service;

import aquadrop_latam.notification_service.models.NotificationTemplate;
import aquadrop_latam.notification_service.dtos.NotificationTemplateDTO;
import aquadrop_latam.notification_service.repository.NotificationTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class NotificationTemplateService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationTemplateService.class);
    
    @Autowired
    private NotificationTemplateRepository templateRepository;
    
    public NotificationTemplate createTemplate(NotificationTemplateDTO dto) {
        NotificationTemplate template = NotificationTemplate.builder()
                .id(UUID.randomUUID().toString())
                .code(dto.getCode())
                .channel(dto.getChannel())
                .subject(dto.getSubject())
                .body(dto.getBody())
                .active(dto.isActive())
                .description(dto.getDescription())
                .build();
        
        NotificationTemplate saved = templateRepository.save(template);
        logger.info("Template created: {} with code: {}", saved.getId(), saved.getCode());
        return saved;
    }
    
    public NotificationTemplate getTemplateById(String id) {
        return templateRepository.findById(id).orElse(null);
    }
    
    public NotificationTemplate getTemplateByCode(String code) {
        return templateRepository.findByCode(code).orElse(null);
    }
    
    public List<NotificationTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }
    
    public List<NotificationTemplate> getActiveTemplates() {
        return templateRepository.findByActive(true);
    }
    
    public NotificationTemplate updateTemplate(String id, NotificationTemplateDTO dto) {
        Optional<NotificationTemplate> template = templateRepository.findById(id);
        
        if (template.isPresent()) {
            NotificationTemplate t = template.get();
            t.setCode(dto.getCode());
            t.setChannel(dto.getChannel());
            t.setSubject(dto.getSubject());
            t.setBody(dto.getBody());
            t.setActive(dto.isActive());
            t.setDescription(dto.getDescription());
            
            NotificationTemplate updated = templateRepository.save(t);
            logger.info("Template updated: {}", id);
            return updated;
        }
        
        return null;
    }
    
    public boolean deleteTemplate(String id) {
        if (templateRepository.existsById(id)) {
            templateRepository.deleteById(id);
            logger.info("Template deleted: {}", id);
            return true;
        }
        return false;
    }
}
