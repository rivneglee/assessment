package com.stardust.easyassess.assessment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.stardust.easyassess.assessment.dao.repositories"})
public class AssessServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssessServiceApplication.class, args);
	}
}
