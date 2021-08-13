package com.ramble.identity.security

import com.ramble.identity.common.*
import com.ramble.identity.models.Roles
import com.ramble.identity.service.UserInfoService
import com.ramble.token.AuthTokensService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.codec.json.AbstractJackson2Decoder
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
@EnableWebFlux
@EnableWebFluxSecurity
class SecurityConfiguration : WebFluxConfigurer {

    private val swaggerWhiteList = arrayOf(
        "/swagger-resources",
        "/swagger-resources/**",
        "/configuration/ui",
        "/configuration/security",
        "/swagger-ui.html",
        "/webjars/**",
        "/v3/api-docs/**",
        "/swagger-ui/**"
    )

    @Bean
    fun configureSecurity(
        httpSecurity: ServerHttpSecurity,
        authenticationWebFilter: AuthenticationWebFilter,
        authTokensService: AuthTokensService
    ): SecurityWebFilterChain =
        httpSecurity
            .csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .logout().disable()
            .authorizeExchange()
            .pathMatchers(*swaggerWhiteList).permitAll()
            .pathMatchers(HttpMethod.POST, SIGN_UP_URL).permitAll()
            .pathMatchers(HttpMethod.GET, SIGN_UP_CONFIRMATION_URL).permitAll()
            .pathMatchers(HttpMethod.POST, REFRESH_TOKEN_URL).permitAll()
            .pathMatchers("$AUTH_API_BASE_PATH/**").hasRole(Roles.User.toString())
            .pathMatchers("$USER_INFO_API_BASE_PATH/**").hasRole(Roles.User.toString())
            .anyExchange().authenticated()
            .and()
            .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .addFilterAt(AuthorizationConverter(authTokensService), SecurityWebFiltersOrder.AUTHORIZATION)
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .build()

    @Bean
    fun bCryptPasswordEncoder(): PasswordEncoder = BCryptPasswordEncoder(10)

    @Bean
    fun jacksonDecoder(): AbstractJackson2Decoder = Jackson2JsonDecoder()

    @Bean
    fun reactiveAuthenticationManager(
        reactiveUserDetailsService: UserInfoService,
        passwordEncoder: PasswordEncoder
    ): ReactiveAuthenticationManager =
        UserDetailsRepositoryReactiveAuthenticationManager(reactiveUserDetailsService).apply {
            setPasswordEncoder(passwordEncoder)
        }

    @Bean
    fun authenticationWebFilter(
        reactiveAuthenticationManager: ReactiveAuthenticationManager,
        authenticationConverter: AuthenticationConverter,
        authenticationSuccessHandler: AuthenticationSuccessHandler,
        authenticationFailureHandler: AuthenticationFailureHandler
    ): AuthenticationWebFilter =
        AuthenticationWebFilter(reactiveAuthenticationManager).apply {
            setRequiresAuthenticationMatcher {
                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, LOG_IN_URL).matches(it)
            }
            setServerAuthenticationConverter(authenticationConverter)
            setAuthenticationSuccessHandler(authenticationSuccessHandler)
            setAuthenticationFailureHandler(authenticationFailureHandler)
            setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        }

}