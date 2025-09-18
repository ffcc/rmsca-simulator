package py.una.pol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "py.una.pol.repository")
public class StaticBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(StaticBackendApplication.class, args);
	}

}
