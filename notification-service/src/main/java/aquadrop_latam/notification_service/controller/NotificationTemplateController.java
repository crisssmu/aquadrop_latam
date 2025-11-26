package aquadrop_latam.notification_service.controller;

import aquadrop_latam.notification_service.service.NotificationTemplateService;
import aquadrop_latam.notification_service.models.NotificationTemplate;
import aquadrop_latam.notification_service.dtos.NotificationTemplateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/templates")
@CrossOrigin(origins = "*")
public class NotificationTemplateController {
    
    @Autowired
    private NotificationTemplateService templateService;
    
    @PostMapping
    public ResponseEntity<NotificationTemplate> createTemplate(@Valid @RequestBody NotificationTemplateDTO dto) {
        NotificationTemplate template = templateService.createTemplate(dto);
        return new ResponseEntity<>(template, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NotificationTemplate> getTemplateById(@PathVariable String id) {
        NotificationTemplate template = templateService.getTemplateById(id);
        if (template != null) {
            return ResponseEntity.ok(template);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<NotificationTemplate> getTemplateByCode(@PathVariable String code) {
        NotificationTemplate template = templateService.getTemplateByCode(code);
        if (template != null) {
            return ResponseEntity.ok(template);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping
    public ResponseEntity<List<NotificationTemplate>> getAllTemplates() {
        List<NotificationTemplate> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<NotificationTemplate> updateTemplate(@PathVariable String id, 
                                                                @Valid @RequestBody NotificationTemplateDTO dto) {
        NotificationTemplate template = templateService.updateTemplate(id, dto);
        if (template != null) {
            return ResponseEntity.ok(template);
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String id) {
        if (templateService.deleteTemplate(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
