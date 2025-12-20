package MeowMeowPunch.pickeat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan // @ConfigurationProperties 어노테이션이 붙은 클래스를 자동 스캔
public class PickeatApplication {

	public static void main(String[] args) {
		SpringApplication.run(PickeatApplication.class, args);
	}

}
