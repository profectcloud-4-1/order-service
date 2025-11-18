package profect.group1.goormdotcom.common.config;

import profect.group1.goormdotcom.common.security.UserHeaderAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public UserHeaderAuthenticationFilter userHeaderAuthenticationFilter() {
        return new UserHeaderAuthenticationFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            UserHeaderAuthenticationFilter userHeaderAuthenticationFilter
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/internal/**").permitAll() // 내부 서비스 간 통신용 (부하 테스트 포함)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(userHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}