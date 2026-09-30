package pe.cibertec.melodicvault.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import pe.cibertec.melodicvault.service.UsuarioDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired private UsuarioDetailsService uds;

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
        		.requestMatchers("/css/**", "/img/**", "/js/**").permitAll()
        		.requestMatchers("/", "/acerca", "/bandas", "/albumes", "/canciones", "/*/detalle/**").permitAll()
        		.requestMatchers("/*/nuevo", "/*/editar/**", "/*/guardar", "/*/eliminar/**", "/albumes/completo/**").hasRole("ADMIN")
        		.anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login").permitAll())
            .logout(l -> l.logoutSuccessUrl("/"));
        return http.build();
    }
}