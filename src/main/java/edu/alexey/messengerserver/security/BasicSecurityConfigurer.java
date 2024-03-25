package edu.alexey.messengerserver.security;

import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("default")
public class BasicSecurityConfigurer {

	@Bean
	PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance(); //new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.csrf(conf -> conf.disable())
				.authorizeHttpRequests(request -> request.anyRequest().authenticated())
				//.authorizeHttpRequests(request -> request.anyRequest().permitAll())
				//				.formLogin(login -> login // enable form based log in
				//						// set permitAll for all URLs associated with Form Login
				//						.permitAll())
				.httpBasic(Customizer.withDefaults())
				.build();
	}

	@Bean
	UserDetailsService userDetailsService(PasswordEncoder encoder) {
		List<UserDetails> users = List.of(
				new UserDetails() {

					private static final long serialVersionUID = 1L;

					@Override
					public Collection<? extends GrantedAuthority> getAuthorities() {
						return List.of(new SimpleGrantedAuthority("ROLE_USER"));
					}

					@Override
					public String getPassword() {
						return encoder.encode("pass");
					}

					@Override
					public String getUsername() {
						return "buzz";
					}

					@Override
					public boolean isAccountNonExpired() {
						return true;
					}

					@Override
					public boolean isAccountNonLocked() {
						return true;
					}

					@Override
					public boolean isCredentialsNonExpired() {
						return true;
					}

					@Override
					public boolean isEnabled() {
						return true;
					}

				});

		return new InMemoryUserDetailsManager(users);
	}
}
