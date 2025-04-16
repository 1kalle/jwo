package com.example.jwo.security;

import com.example.jwo.repository.UserRepository;
import com.example.jwo.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity // <1>
@Configuration
public class SecurityConfig extends VaadinWebSecurity { // <2>


    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /* TÄllä toimi
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth ->
                auth.requestMatchers(
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/images/*.png")).permitAll()
        );  // <3>
        super.configure(http);
        setLoginView(http, LoginView.class); // <4>
    }
    */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf(csrf ->csrf.ignoringRequestMatchers("/api/admin/**")).
        authorizeHttpRequests(auth ->
                auth.requestMatchers(
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/images/*.png")).permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/admin/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/**").permitAll()

        );
        super.configure(http);
        setLoginView(http, LoginView.class);
    }



    /*
    @Bean
    public UserDetailsService users() {
        UserDetails user = User.builder()
                .username("user")
                // password = password with this hash, don't tell anybody :-)
                .password("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
                .roles("USER")
                .build();
        UserDetails admin = User.builder()
                .username("admin")
                .password("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
                .roles("USER", "ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user, admin); // <5>
    }
    */

    // Define the `UserDetailsService` to load users from the database
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            com.example.jwo.entity.User user = userRepository.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }
            return User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole())
                    .build();
        };
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}