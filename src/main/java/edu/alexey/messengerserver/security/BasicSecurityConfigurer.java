package edu.alexey.messengerserver.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import edu.alexey.messengerserver.entities.User;
import edu.alexey.messengerserver.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@Slf4j
@Profile("default")
public class BasicSecurityConfigurer {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(); // NoOpPasswordEncoder.getInstance(); 
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.csrf(conf -> conf.disable())
				.authorizeHttpRequests(request -> request
						.requestMatchers(HttpMethod.GET, "/hello", "/users/{user_uuid}").permitAll()
						.requestMatchers(HttpMethod.POST, "/users/signup").permitAll()
						.requestMatchers(HttpMethod.GET, "/users*").authenticated()
						.requestMatchers("/messages/**").authenticated()
						.requestMatchers("/client", "/client/**").authenticated()
						.anyRequest().denyAll())
				//.authorizeHttpRequests(request -> request.anyRequest().permitAll())
				//				.formLogin(login -> login // enable form based log in
				//						// set permitAll for all URLs associated with Form Login
				//						.permitAll())
				.httpBasic(Customizer.withDefaults())
				.build();
	}

	@Bean
	UserDetailsService userDetailsService(UserRepository userRepository) {

		return new UserDetailsService() {

			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				User user = userRepository.findByUsername(username);
				if (user == null) {
					log.error("User {} not found", username);
					throw new UsernameNotFoundException(username);
				}
				return user;
			}
		};

		//		List<UserDetails> users = List.of(
		//				new UserDetails() {
		//
		//					private static final long serialVersionUID = 1L;
		//
		//					@Override
		//					public Collection<? extends GrantedAuthority> getAuthorities() {
		//						return List.of(new SimpleGrantedAuthority("ROLE_USER"));
		//					}
		//
		//					@Override
		//					public String getPassword() {
		//						return encoder.encode("strongpass");
		//					}
		//
		//					@Override
		//					public String getUsername() {
		//						return "buzz";
		//					}
		//
		//					@Override
		//					public boolean isAccountNonExpired() {
		//						return true;
		//					}
		//
		//					@Override
		//					public boolean isAccountNonLocked() {
		//						return true;
		//					}
		//
		//					@Override
		//					public boolean isCredentialsNonExpired() {
		//						return true;
		//					}
		//
		//					@Override
		//					public boolean isEnabled() {
		//						return true;
		//					}
		//
		//				});
		//
		//		return new InMemoryUserDetailsManager(users);
	}
}
