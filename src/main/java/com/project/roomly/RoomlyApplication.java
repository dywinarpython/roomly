package com.project.roomly;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@OpenAPIDefinition(
		info = @Info(
				title = "Roomly API",
				version = "1.0",
				description = "Документация API"
		),
		security = @SecurityRequirement(name = "oauth2")
)
@SecurityScheme(
		name = "oauth2",
		type = SecuritySchemeType.OAUTH2,
		flows = @OAuthFlows(
				authorizationCode = @OAuthFlow(
						authorizationUrl = "${KEYCLOAK_URI:http://localhost:8080/realms/roomly}/protocol/openid-connect/auth",
						tokenUrl         = "${KEYCLOAK_URI:http://localhost:8080/realms/roomly}/protocol/openid-connect/token",
						scopes = {
								@OAuthScope(name = "openid",  description = "OpenID scope"),
								@OAuthScope(name = "profile", description = "User profile")
						}
				)
		)
)
@EnableScheduling
@SpringBootApplication
public class RoomlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomlyApplication.class, args);
	}

}
