package com.ramble.accesstoken.handler

import com.ramble.accesstoken.handler.helper.TokenClaimsMapGenerator
import com.ramble.accesstoken.handler.helper.TokenDurationGenerator
import com.ramble.accesstoken.model.AccessClaims
import com.ramble.accesstoken.model.TokenDuration
import io.jsonwebtoken.*
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccessTokenHandlerTest {

    private val jwtKey = mock(SecretKey::class.java)
    private val tokenDurationGenerator = mock(TokenDurationGenerator::class.java)
    private val tokenClaimsMapGenerator = mock(TokenClaimsMapGenerator::class.java)

    private val parser = mock(JwtParser::class.java)
    private val now = Instant.now()
    private val accessTokenStr = "some_access_token"
    private val claims = mock(Claims::class.java)
    private val emailId = "someEmailId@random.com"
    private val userId = "someUserId"
    private val clientId = "someClientId"

    private val accessTokenHandler by lazy {
        AccessTokenHandler(
            jwtKey,
            tokenDurationGenerator,
            tokenClaimsMapGenerator
        )
    }

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
        given(
            tokenClaimsMapGenerator.getAccessTokenClaimsMap(
                clientId,
                userId,
                authorities
            )
        ).willReturn(claimsMap)
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
                .generateToken(
                    authorities,
                    clientId,
                    userId,
                    emailId,
                    issuedInstant,
                    expiryDurationAmount,
                    expiryDurationUnit,
                    jwtBuilder
                )
        )
    }

    @Test
    fun `getAccessClaims should return null if passed token is expired`() {
        // Stub
        given(claims.expiration).willReturn(Date.from(now.minus(10, ChronoUnit.MINUTES)))

        // Call method and assert
        assertNull(accessTokenHandler.getAccessClaims(accessTokenStr, parser, now))
    }

    @Test
    fun `getAccessClaims should return null if passed token does not have userId`() {
        // Stub
        given(claims.expiration).willReturn(Date.from(now.plus(10, ChronoUnit.MINUTES)))
        given(claims.subject).willReturn(emailId)
        given(claims["USER_ID"]).willReturn(null)
        given(claims["CLIENT_ID"]).willReturn(clientId)

        // Call method and assert
        assertNull(accessTokenHandler.getAccessClaims(accessTokenStr, parser, now))
    }

    @Test
    fun `getAccessClaims should return null if passed token does not have clientId`() {
        // Stub
        given(claims.expiration).willReturn(Date.from(now.plus(10, ChronoUnit.MINUTES)))
        given(claims.subject).willReturn(emailId)
        given(claims["USER_ID"]).willReturn(userId)
        given(claims["CLIENT_ID"]).willReturn(null)

        // Call method and assert
        assertNull(accessTokenHandler.getAccessClaims(accessTokenStr, parser, now))
    }

    @Test
    fun `getAccessClaims should return null if passed token does not have authorities`() {
        // Stub
        given(claims.expiration).willReturn(Date.from(now.plus(10, ChronoUnit.MINUTES)))
        given(claims.subject).willReturn(emailId)
        given(claims["USER_ID"]).willReturn(userId)
        given(claims["CLIENT_ID"]).willReturn(clientId)
        given(claims["ROLES"]).willReturn(null)

        // Call method and assert
        assertNull(accessTokenHandler.getAccessClaims(accessTokenStr, parser, now))
    }

    @Test
    fun `getAccessClaims should return valid AccessClaims if valid token`() {
        val expiredInstant = now.plus(10, ChronoUnit.MINUTES)
        val roles = listOf("user", "admin")

        // Stub
        given(claims.expiration).willReturn(Date.from(expiredInstant))
        given(claims.subject).willReturn(emailId)
        given(claims["USER_ID"]).willReturn(userId)
        given(claims["CLIENT_ID"]).willReturn(clientId)
        given(claims["ROLES"]).willReturn(roles)

        // Call method and assert
        val claimsResult = AccessClaims(clientId, userId, emailId, claims, roles)
        assertEquals(claimsResult, accessTokenHandler.getAccessClaims(accessTokenStr, parser, now))
    }

    @Test
    fun `getAccessClaims should return valid AccessClaims if valid claims and authorities`() {
        val roles = listOf("user", "admin")
        val authorities = roles.map { SimpleGrantedAuthority(it) }

        // Stub
        given(claims.subject).willReturn(emailId)
        given(claims["USER_ID"]).willReturn(userId)
        given(claims["CLIENT_ID"]).willReturn(clientId)

        // Call method and assert
        val claimsResult = AccessClaims(clientId, userId, emailId, claims, roles)
        assertEquals(claimsResult, accessTokenHandler.getAccessClaims(claims, authorities))
    }

    @Test
    fun `isValidToken should return false if passed token is expired`() {
        // Stub
        given(claims.expiration).willReturn(Date.from(now.minus(10, ChronoUnit.MINUTES)))
        given(claims.subject).willReturn(emailId)
        given(claims["CLIENT_ID"]).willReturn(clientId)

        // Call method and assert
        assertFalse(accessTokenHandler.isValidToken(accessTokenStr, parser, now))
    }

    @Test
    fun `isValidToken should return false if passed token does not have email`() {
        // Stub
        given(claims.expiration).willReturn(Date.from(now.plus(10, ChronoUnit.MINUTES)))
        given(claims.subject).willReturn(null)
        given(claims["CLIENT_ID"]).willReturn(clientId)

        // Call method and assert
        assertFalse(accessTokenHandler.isValidToken(accessTokenStr, parser, now))
    }

    @Test
    fun `isValidToken should return false if passed token does not have clientId`() {
        // Stub
        given(claims.expiration).willReturn(Date.from(now.plus(10, ChronoUnit.MINUTES)))
        given(claims.subject).willReturn(emailId)
        given(claims["CLIENT_ID"]).willReturn(null)

        // Call method and assert
        assertFalse(accessTokenHandler.isValidToken(accessTokenStr, parser, now))
    }

    @Test
    fun `isValidToken should return true if valid token`() {
        // Stub
        given(claims.expiration).willReturn(Date.from(now.plus(10, ChronoUnit.MINUTES)))
        given(claims.subject).willReturn(emailId)
        given(claims["CLIENT_ID"]).willReturn(clientId)

        // Call method and assert
        assertTrue(accessTokenHandler.isValidToken(accessTokenStr, parser, now))
    }

    @Test
    fun `getUserIdFromToken when claims do not have userId`() {
        given(claims["USER_ID"]).willReturn(null)
        assertNull(accessTokenHandler.getUserIdFromToken(accessTokenStr, parser))
    }

    @Test
    fun `getUserIdFromToken when claims have userId`() {
        given(claims["USER_ID"]).willReturn(userId)
        assertEquals(userId, accessTokenHandler.getUserIdFromToken(accessTokenStr, parser))
    }

    @Test
    fun `getClientIdFromToken when claims do not have clientId`() {
        given(claims["CLIENT_ID"]).willReturn(null)
        assertNull(accessTokenHandler.getClientIdFromToken(accessTokenStr, parser))
    }

    @Test
    fun `getClientIdFromToken when claims have clientId`() {
        given(claims["CLIENT_ID"]).willReturn(clientId)
        assertEquals(clientId, accessTokenHandler.getClientIdFromToken(accessTokenStr, parser))
    }

    @Test
    fun `getEmailFromToken when claims do not have clientId`() {
        given(claims.subject).willReturn(null)
        assertNull(accessTokenHandler.getEmailFromToken(accessTokenStr, parser))
    }

    @Test
    fun `getEmailFromToken when claims have clientId`() {
        given(claims.subject).willReturn(emailId)
        assertEquals(emailId, accessTokenHandler.getEmailFromToken(accessTokenStr, parser))
    }

}