package aquadrop_latam.fleet_service.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "aquadrop_latam.fleet_service.repository")
@EntityScan(basePackages = "aquadrop_latam.fleet_service.models")
public class JpaConfiguration {
}
