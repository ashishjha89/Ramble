package com.ramble.identity.security

import com.ramble.identity.common.SIGN_UP_CONFIRMATION_URL
import com.ramble.identity.common.SIGN_UP_URL
import com.ramble.identity.common.USER_INFO_API_BASE_PATH
import com.ramble.identity.common.USER_LOGIN_PATH
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

    @Throws(Exception::class)
    override fun configure(httpSecurity: HttpSecurity) {
        httpSecurity
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPointInterceptor)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests().antMatchers(HttpMethod.POST, SIGN_UP_URL).permitAll()
                .and()
                .authorizeRequests().antMatchers(HttpMethod.GET, SIGN_UP_CONFIRMATION_URL).permitAll()
                .antMatchers("$USER_INFO_API_BASE_PATH/**").hasRole(Roles.User.toString())
                .anyRequest().authenticated()
                .and()
                .addFilter(
                        AuthenticationFilter(
                                manager = authenticationManager(),
                                authTokensService = authTokensService,
                                userInfoService = userInfoService,
                                loginPath = "$USER_INFO_API_BASE_PATH/$USER_LOGIN_PATH"
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