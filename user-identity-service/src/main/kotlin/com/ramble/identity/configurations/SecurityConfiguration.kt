package com.ramble.identity.configurations

import com.ramble.identity.common.SIGN_UP_URL
import com.ramble.identity.common.USER_INFO_API_BASE_PATH
import com.ramble.identity.common.USER_INFO_LOGIN_PATH
import com.ramble.identity.models.Roles
import com.ramble.identity.service.UserService
import com.ramble.token.handler.TokensHandler
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@EnableWebSecurity
class SecurityConfiguration(
        private val userService: UserService,
        private val bCryptPasswordEncoder: BCryptPasswordEncoder,
        private val authenticationEntryPointImpl: AuthenticationEntryPointImpl,
        private val tokensHandler: TokensHandler
) : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(httpSecurity: HttpSecurity) {
        httpSecurity
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPointImpl)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests().antMatchers(HttpMethod.POST, SIGN_UP_URL).permitAll()
                .antMatchers("$USER_INFO_API_BASE_PATH/**").hasRole(Roles.Active.toString())
                .anyRequest().authenticated()
                .and()
                .addFilter(
                        AuthenticationFilter(
                                manager = authenticationManager(),
                                tokensHandler = tokensHandler,
                                userService = userService,
                                loginPath = "$USER_INFO_API_BASE_PATH/$USER_INFO_LOGIN_PATH"
                        )
                )
                .addFilter(
                        AuthorizationFilter(
                                authManager = authenticationManager(),
                                tokensHandler = tokensHandler
                        )
                )
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder)
    }
}