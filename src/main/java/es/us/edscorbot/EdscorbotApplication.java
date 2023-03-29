package es.us.edscorbot;

import java.sql.SQLException;

import org.h2.tools.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class,ManagementWebSecurityAutoConfiguration.class})
public class EdscorbotApplication {

	public static void main(String[] args) {

		try {
			Server h2Server = Server.createTcpServer().start();
			if (h2Server.isRunning(true)) {
				System.out.println("H2 server available on port: " + h2Server.getStatus());
			} else {
				throw new RuntimeException("Could not start H2 server.");
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to start H2 server: ", e);
		}

		SpringApplication.run(EdscorbotApplication.class, args);
	}

}