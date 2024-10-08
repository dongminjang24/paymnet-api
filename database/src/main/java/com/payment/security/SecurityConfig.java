package com.payment.security;

import java.util.Arrays;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {


	private final org.springframework.core.env.Environment env;
	private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
	private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
	private final CustomLoginAuthenticationEntryPoint authenticationEntryPoint;
	private final AuthenticationConfiguration authenticationConfiguration;
	private final CustomAccessDeniedHandler accessDeniedHandler;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults())
			.headers(headers -> {
				if (Arrays.asList(env.getActiveProfiles()).contains("dev")) {
					headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable);
				} else {
					headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin);
				}
			})
			.authorizeHttpRequests(authorize -> {
				authorize
					.requestMatchers("/swagger-ui/**", "/swagger-ui/index.html", "/api-docs/**", "/webjars/**","/payment/**",
						"/static/**", "/auth/**", "/main/**", "/api/v1/member/signup", "/api/v1/member/signIn","/favicon.ico"
					,"api/v1/notification/subscribe","sse-test.html"
					)
					.permitAll();
					// .requestMatchers("/api/v1/payments/toss")
					// .hasRole("USER");

				// H2 콘솔 설정을 개발 환경에서만 적용
				if (Arrays.asList(env.getActiveProfiles()).contains("dev")) {
					authorize.requestMatchers(PathRequest.toH2Console()).permitAll();
				}

				authorize.anyRequest().permitAll();
			})
			.formLogin(login -> login
				.loginProcessingUrl("/api/v1/member/signIn")
				.usernameParameter("email")
				.passwordParameter("password")
				.successHandler(customAuthenticationSuccessHandler)
				.failureHandler(customAuthenticationFailureHandler)
			)
			.logout(logout -> logout
				.logoutUrl("/api/v1/member/signOut")
				.logoutSuccessHandler((request, response, authentication) -> {
					response.setStatus(HttpServletResponse.SC_OK);
				})
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID")
			)
			.sessionManagement(session -> session
				.maximumSessions(1)
				.maxSessionsPreventsLogin(true)
			)
			.addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class)
			.exceptionHandling(config -> config
				.authenticationEntryPoint(authenticationEntryPoint)
				.accessDeniedHandler(accessDeniedHandler));


		return http.build();
	}



	@Bean
	public BCryptPasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CustomAuthenticationFilter authenticationFilter() throws Exception {
		CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter();
		customAuthenticationFilter.setAuthenticationManager(authenticationManager());
		customAuthenticationFilter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler);
		customAuthenticationFilter.setAuthenticationFailureHandler(customAuthenticationFailureHandler);

		// **
		customAuthenticationFilter.setSecurityContextRepository(
			new DelegatingSecurityContextRepository(
				new RequestAttributeSecurityContextRepository(),
				new HttpSessionSecurityContextRepository()
			));

		return customAuthenticationFilter;
	}

	@Bean
	public AuthenticationManager authenticationManager() throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}


}
