package aquadrop_latam.bookingService;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class BookingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingServiceApplication.class, args);
	}

	@Component
	public static class DataInitializer {
		private final DataSource dataSource;

		public DataInitializer(DataSource dataSource) {
			this.dataSource = dataSource;
		}

		@EventListener(ContextRefreshedEvent.class)
		public void initializeData() {
			System.out.println("=== DataInitializer.initializeData CALLED ===");
			try {
				// Wait for Hibernate to create tables
				Thread.sleep(2000);
				System.out.println("Connection timeout exceeded, proceeding...");
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			
			try (Connection conn = dataSource.getConnection();
				 Statement stmt = conn.createStatement()) {
				
				System.out.println("Database connected. Inserting priority tags...");
				
				// Insert priority tags after Hibernate creates tables
				String[] insertStatements = {
					"INSERT INTO priority_tag (id, score, type) VALUES (1, 1, 'DEFAULT') ON CONFLICT (id) DO NOTHING",
					"INSERT INTO priority_tag (id, score, type) VALUES (2, 3, 'HOSPITAL') ON CONFLICT (id) DO NOTHING",
					"INSERT INTO priority_tag (id, score, type) VALUES (3, 2, 'ESCUELA') ON CONFLICT (id) DO NOTHING",
					"INSERT INTO priority_tag (id, score, type) VALUES (4, 4, 'VULNERABLE') ON CONFLICT (id) DO NOTHING"
				};
				
				for (String sql : insertStatements) {
					try {
						stmt.execute(sql);
						System.out.println("✓ Inserted: " + sql);
					} catch (SQLException e) {
						System.out.println("! Skipped: " + e.getMessage());
					}
				}
				System.out.println("=== DataInitializer COMPLETED ===");
			} catch (SQLException e) {
				System.err.println("ERROR initializing priority tags: " + e.getMessage());
			}
		}
	}

}
