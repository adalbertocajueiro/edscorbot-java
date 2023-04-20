package es.us.edscorbot;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class,ManagementWebSecurityAutoConfiguration.class})
public class EdscorbotApplication {


	public static void main(String[] args) {
		SpringApplication.run(EdscorbotApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer(){
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry){

				registry.addMapping("/")
					.allowedHeaders("X-Requested-With", "Origin", "Content-Type", "Accept",
								"Authorization", "Access-Control-Allow-Credentials", "Access-Control-Allow-Headers",
								"Access-Control-Allow-Methods",
								"Access-Control-Allow-Origin", "Access-Control-Expose-Headers",
								"Access-Control-Max-Age",
								"Access-Control-Request-Headers", "Access-Control-Request-Method", "Age", "Allow",
								"Alternates",
								"Authorization",
								"username",
								"Content-Range", "Content-Disposition",
								"Content-Description")
					.allowedOrigins("*")
					.allowedMethods("GET","POST", "PUT","DELETE");
			}
		};
	}

}