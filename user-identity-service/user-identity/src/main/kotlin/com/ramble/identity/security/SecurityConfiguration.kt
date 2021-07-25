package com.ramble.identity.security

import com.ramble.identity.common.*
import com.ramble.identity.models.Roles
import com.ramble.identity.service.UserInfoService
import com.ramble.token.AuthTokensService
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@EnableWebSecurity
class SecurityConfiguration(
        private val userInfoService: UserInfoService,
        private val bCryptPasswordEncoder: BCryptPasswordEncoder,
        private val authenticationEntryPointInterceptor: AuthenticationEntryPointInterceptor,
        private val authTokensService: AuthTokensService
) : WebSecurityConfigurerAdapter() {

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

    @Throws(Exception::class)
    override fun configure(httpSecurity: HttpSecurity) {
        httpSecurity
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPointInterceptor)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests().antMatchers(*swaggerWhiteList).permitAll()
                .and()
                .authorizeRequests().antMatchers(HttpMethod.POST, SIGN_UP_URL).permitAll()
                .and()
                .authorizeRequests().antMatchers(HttpMethod.GET, SIGN_UP_CONFIRMATION_URL).permitAll()
                .and()
                .authorizeRequests().antMatchers(HttpMethod.POST, REFRESH_TOKEN_URL).permitAll()
                .antMatchers("$AUTH_API_BASE_PATH/**").hasRole(Roles.User.toString())
                .anyRequest().authenticated()
                .and()
                .addFilter(
                        AuthenticationFilter(
                                manager = authenticationManager(),
                                authTokensService = authTokensService,
                                userInfoService = userInfoService,
                                loginPath = "$AUTH_API_BASE_PATH/$LOGIN_PATH"
                        )
                )
                .addFilter(
                        AuthorizationFilter(
                                authManager = authenticationManager(),
                                authTokensService = authTokensService
                        )
                )
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userInfoService).passwordEncoder(bCryptPasswordEncoder)
    }
}