package cool.cfapps.apibatchprocessing.config

import cool.cfapps.apibatchprocessing.config.security.ApiKeyAuthenticationFilter
import cool.cfapps.apibatchprocessing.config.security.ApiKeyAuthenticationProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val apiKeyAuthenticationProvider: ApiKeyAuthenticationProvider,
    private val apiKeyAuthenticationFilter: ApiKeyAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/health").permitAll()
                    .requestMatchers("/api/v1/planning/**").authenticated()
                    .anyRequest().permitAll()
            }
            .authenticationProvider(apiKeyAuthenticationProvider)
            .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }




}