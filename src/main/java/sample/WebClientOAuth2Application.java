package sample;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.UnAuthenticatedServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@SpringBootApplication
public class WebClientOAuth2Application {

	@Bean
	InMemoryReactiveClientRegistrationRepository clientRegistrationRepository() {
		ClientRegistration clientRegistration = ClientRegistrations.fromOidcIssuerLocation("http://localhost:8090/uaa/oauth/token")
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.scope("message.read")
				.registrationId("uaa")
				.clientId("general-messaging")
				.clientSecret("secret")
				.build();
		return new InMemoryReactiveClientRegistrationRepository(clientRegistration);
	}

	@Bean
	ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
		return new UnAuthenticatedServerOAuth2AuthorizedClientRepository();
	}

	@Bean
	WebClient webClient(ReactiveClientRegistrationRepository clientRegistrationRepository,
			ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
		ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
				new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository, authorizedClientRepository);
		return WebClient.builder()
				.filter(oauth)
				.build();
	}

	@Bean
	CommandLineRunner demoClientRegistrationId(WebClient webClient) {
		return args -> {
			String body = webClient.get()
				.uri("http://localhost:8092/messages")
				// "uaa" matches the ClientRegistration.registrationId
				.attributes(clientRegistrationId("uaa"))
				.retrieve()
				.bodyToMono(String.class)
				.block();
			System.out.println(body);
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(WebClientOAuth2Application.class, args);
	}
}
