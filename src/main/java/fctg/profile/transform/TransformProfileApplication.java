package fctg.profile.transform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TransformProfileApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransformProfileApplication.class, args);
	}

}
