package com.ramble.token.handler.helper

import io.jsonwebtoken.Claims
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority

internal class UsernamePasswordAuthTokenTokenGenerator {

    fun getUsernamePasswordAuthenticationToken(claims: Claims, authorities: List<GrantedAuthority>) =
            UsernamePasswordAuthenticationToken(claims, null, authorities)
}