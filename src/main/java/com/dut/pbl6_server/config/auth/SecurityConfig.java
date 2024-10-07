package com.dut.pbl6_server.config.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthFilterConfig jwtAuthFilterConfig;

    /**
     * This method is used to configure security for URLs that start with "/api".
     * These URLs are permitted to access without authentication.
     * <p>
     * <b>Reason for permit all: We use method security to control access to the APIs.</b>
     *
     * @param http: The HttpSecurity object that is used to configure security.
     * @return A filter chain that is responsible for all security processing.
     * @throws Exception: If an error occurs.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .securityMatcher(new AntPathRequestMatcher("/api/**"))
            .authorizeHttpRequests(authorize ->
                authorize.anyRequest().permitAll() // permit all so that authenticated with method security
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationManager(authenticationManagerBean())
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilterConfig, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    /**
     * This method is used to configure to permit all requests to WebSocket URLs.
     *
     * @param http: The HttpSecurity object that is used to configure security.
     * @return A filter chain that is responsible for all security processing.
     * @throws Exception: If an error occurs.
     */
    @Bean
    public SecurityFilterChain wsFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .securityMatcher(new AntPathRequestMatcher("/ws/**"))
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .headers(httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
        return http.build();
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
