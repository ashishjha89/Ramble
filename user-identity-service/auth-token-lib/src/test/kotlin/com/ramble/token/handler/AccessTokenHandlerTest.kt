package com.ramble.token.handler

import com.ramble.token.handler.helper.AccessTokenClaimsMapGenerator
import com.ramble.token.handler.helper.TokenDurationGenerator
import com.ramble.token.model.AccessClaims
import com.ramble.token.model.TokenDuration
import io.jsonwebtoken.*
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.security.Principal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AccessTokenHandlerTest {

    private val jwtKey = mock(SecretKey::class.java)
    private val tokenDurationGenerator = mock(TokenDurationGenerator::class.java)
    private val accessTokenClaimsMapGenerator = mock(AccessTokenClaimsMapGenerator::class.java)

    private val parser = mock(JwtParser::class.java)
    private val now = Instant.now()
    private val accessTokenStr = "some_access_token"
    private val claims = mock(Claims::class.java)
    private val emailId = "someEmailId@random.com"
    private val userId = "someUserId"
    private val clientId = "someClientId"

    private val usernamePasswordAuthenticationToken = mock(UsernamePasswordAuthenticationToken::class.java)

    private val accessTokenHandler by lazy { AccessTokenHandler(jwtKey, tokenDurationGenerator, accessTokenClaimsMapGenerator) }

    @Suppress("unchecked_cast")
    @Before
    fun setup() {
        val jwsClaims = mock(Jws::class.java)
        given(parser.parseClaimsJws(accessTokenStr)).willReturn(jwsClaims as Jws<Claims>)
        given(jwsClaims.body).willReturn(claims)
    }

    @Test
    fun generateAccessTokenTest() {
        val jwtBuilder = mock(JwtBuilder::class.java)
        val authResult = mock(Authentication::class.java)
        val authority = mock(GrantedAuthority::class.java)
        val authorities = listOf(authority)

        val issuedInstant = Instant.now()
        val expiryDurationAmount = 30L
        val expiryDurationUnit = ChronoUnit.MINUTES
        val tokenDuration = TokenDuration(
                issuedDate = Date.from(issuedInstant),
                expiryDate = Date.from(issuedInstant.plus(expiryDurationAmount, expiryDurationUnit))
        )
        val claimsMap = mapOf("ROLES" to authorities, "USER_ID" to userId)

        val accessTokenSigned = "some_signed_access_token"

        // Stub
        given(authResult.authorities).willReturn(authorities)
        given(accessTokenClaimsMapGenerator.getAccessTokenClaimsMap(clientId, userId, authorities)).willReturn(claimsMap)
        given(tokenDurationGenerator.getTokenDuration(issuedInstant, expiryDurationAmount, expiryDurationUnit))
                .willReturn(tokenDuration)

        given(jwtBuilder.setClaims(claimsMap)).willReturn(jwtBuilder)
        given(jwtBuilder.setSubject(emailId)).willReturn(jwtBuilder)
        given(jwtBuilder.setIssuedAt(tokenDuration.issuedDate)).willReturn(jwtBuilder)
        given(jwtBuilder.setExpiration(tokenDuration.expiryDate)).willReturn(jwtBuilder)
        given(jwtBuilder.signWith(jwtKey, SignatureAlgorithm.HS512)).willReturn(jwtBuilder)

        given(jwtBuilder.compact()).willReturn(accessTokenSigned)

        // Call method and assert
        assertEquals(
                accessTokenSigned,
                accessTokenHandler
                        .generateAccessToken(
                                authorities,
                                clientId,
                                userId,
                                emailId,
                                issuedInstant,
                                expiryDurationAmount,
                                expiryDurationUnit,
                                jwtBuilder))
    }

    @Test
    fun `getTokenClaims should return null if passed token=null`() {
        assertNull(accessTokenHandler.getTokenClaims(null, parser, now))
    }

    @Test
    fun `getTokenClaims should return null if passed token is expired`() {
        val expiredInstant = now.minus(10, ChronoUnit.MINUTES)

        // Stub
        given(claims.expiration).willReturn(Date.from(expiredInstant))

        // Call method and assert
        assertNull(accessTokenHandler.getTokenClaims(accessTokenStr, parser, now))
    }

    @Test
    fun `getTokenClaims should return null if passed token does not have userId`() {
        val expiredInstant = now.plus(10, ChronoUnit.MINUTES)

        // Stub
        given(claims.expiration).willReturn(Date.from(expiredInstant))
        given(claims.subject).willReturn(emailId)
        given(claims["USER_ID"]).willReturn(null)
        given(claims["CLIENT_ID"]).willReturn(clientId)

        // Call method and assert
        assertNull(accessTokenHandler.getTokenClaims(accessTokenStr, parser, now))
    }

    @Test
    fun `getTokenClaims should return null if passed token does not have clientId`() {
        val expiredInstant = now.plus(10, ChronoUnit.MINUTES)

        // Stub
        given(claims.expiration).willReturn(Date.from(expiredInstant))
        given(claims.subject).willReturn(emailId)
        given(claims["USER_ID"]).willReturn(userId)
        given(claims["CLIENT_ID"]).willReturn(null)

        // Call method and assert
        assertNull(accessTokenHandler.getTokenClaims(accessTokenStr, parser, now))
    }

    @Test
    fun `getTokenClaims should return null if passed token does not have authorities`() {
        val expiredInstant = now.plus(10, ChronoUnit.MINUTES)

        // Stub
        given(claims.expiration).willReturn(Date.from(expiredInstant))
        given(claims.subject).willReturn(emailId)
        given(claims["USER_ID"]).willReturn(userId)
        given(claims["CLIENT_ID"]).willReturn(clientId)
        given(claims["ROLES"]).willReturn(null)

        // Call method and assert
        assertNull(accessTokenHandler.getTokenClaims(accessTokenStr, parser, now))
    }

    @Test
    fun `getTokenClaims should return valid AccessClaims if valid token`() {
        val expiredInstant = now.plus(10, ChronoUnit.MINUTES)
        val roles = listOf("user", "admin")
        val authorities: List<SimpleGrantedAuthority> = roles.map { SimpleGrantedAuthority(it) }

        // Stub
        given(claims.expiration).willReturn(Date.from(expiredInstant))
        given(claims.subject).willReturn(emailId)
        given(claims["USER_ID"]).willReturn(userId)
        given(claims["CLIENT_ID"]).willReturn(clientId)
        given(claims["ROLES"]).willReturn(roles)

        // Call method and assert
        val claimsResult = AccessClaims(clientId, userId, emailId, claims, authorities)
        assertEquals(claimsResult, accessTokenHandler.getTokenClaims(accessTokenStr, parser, now))
    }

    @Test
    fun `getPrincipalClaims should return null if principal is not UsernamePasswordAuthenticationToken`() {
        val principal = mock(Principal::class.java)
        assertNull(accessTokenHandler.getPrincipalClaims(principal))
    }

    @Test
    fun `getPrincipalClaims should return null if principal does not have authorities`() {
        // Stub
        given(usernamePasswordAuthenticationToken.principal).willReturn(claims)
        given(usernamePasswordAuthenticationToken.authorities).willReturn(null)

        // Call method and assert
        assertNull(accessTokenHandler.getPrincipalClaims(usernamePasswordAuthenticationToken))
    }

    @Test
    fun `getPrincipalClaims should return valid AccessClaims if valid principal`() {
        val authorities = listOf("user", "admin").map { SimpleGrantedAuthority(it) }

        // Stub
        given(usernamePasswordAuthenticationToken.principal).willReturn(claims)
        given(usernamePasswordAuthenticationToken.authorities).willReturn(authorities)
        given(claims.subject).willReturn(emailId)
        given(claims["USER_ID"]).willReturn(userId)
        given(claims["CLIENT_ID"]).willReturn(clientId)

        // Call method and assert
        val claimsResult = AccessClaims(clientId, userId, emailId, claims, authorities)
        assertEquals(claimsResult, accessTokenHandler.getPrincipalClaims(usernamePasswordAuthenticationToken))
    }
}