package com.ramble.token.handler

import com.ramble.token.handler.helper.TokenClaimsMapGenerator
import com.ramble.token.handler.helper.TokenDurationGenerator
import com.ramble.token.model.TokenDuration
import io.jsonwebtoken.*
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RefreshTokenHandlerTest {

    private val jwtKey = mock(SecretKey::class.java)
    private val tokenDurationGenerator = mock(TokenDurationGenerator::class.java)
    private val tokenClaimsMapGenerator = mock(TokenClaimsMapGenerator::class.java)

    private val parser = mock(JwtParser::class.java)
    private val now = Instant.now()
    private val refreshTokenStr = "some_refresh_token"
    private val claims = mock(Claims::class.java)
    private val userId = "someUserId"
    private val clientId = "someClientId"

    private val refreshTokenHandler by lazy {
        RefreshTokenHandler(
            jwtKey,
            tokenDurationGenerator,
            tokenClaimsMapGenerator
        )
    }

    @Suppress("unchecked_cast")
    @Before
    fun setup() {
        val jwsClaims = mock(Jws::class.java)
        given(parser.parseClaimsJws(refreshTokenStr)).willReturn(jwsClaims as Jws<Claims>)
        given(jwsClaims.body).willReturn(claims)
    }

    @Test
    fun generateTokenTest() {
        val jwtBuilder = mock(JwtBuilder::class.java)
        val issuedInstant = Instant.now()
        val expiryDurationAmount = 30L
        val expiryDurationUnit = ChronoUnit.MINUTES
        val tokenDuration = TokenDuration(
            issuedDate = Date.from(issuedInstant),
            expiryDate = Date.from(issuedInstant.plus(expiryDurationAmount, expiryDurationUnit))
        )
        val claimsMap = mapOf("USER_ID" to userId, "CLIENT_ID" to clientId)

        val refreshTokenSigned = "some_signed_refresh_token"

        // Stub
        given(tokenClaimsMapGenerator.getRefreshTokenClaimsMap(clientId, userId)).willReturn(claimsMap)
        given(tokenDurationGenerator.getTokenDuration(issuedInstant, expiryDurationAmount, expiryDurationUnit))
            .willReturn(tokenDuration)

        given(jwtBuilder.setClaims(claimsMap)).willReturn(jwtBuilder)
        given(jwtBuilder.setSubject(userId)).willReturn(jwtBuilder)
        given(jwtBuilder.setIssuedAt(tokenDuration.issuedDate)).willReturn(jwtBuilder)
        given(jwtBuilder.setExpiration(tokenDuration.expiryDate)).willReturn(jwtBuilder)
        given(jwtBuilder.signWith(jwtKey, SignatureAlgorithm.HS512)).willReturn(jwtBuilder)

        given(jwtBuilder.compact()).willReturn(refreshTokenSigned)

        // Call method and assert
        assertEquals(
            refreshTokenSigned,
            refreshTokenHandler
                .generateToken(clientId, userId, issuedInstant, expiryDurationAmount, expiryDurationUnit, jwtBuilder)
        )
    }

    @Test
    fun `getUserIdFromToken when claims do not have userId`() {
        given(claims["USER_ID"]).willReturn(null)
        assertNull(refreshTokenHandler.getUserIdFromToken(refreshTokenStr, parser))
    }

    @Test
    fun `getUserIdFromToken when claims have userId`() {
        given(claims["USER_ID"]).willReturn(userId)
        assertEquals(userId, refreshTokenHandler.getUserIdFromToken(refreshTokenStr, parser))
    }

    @Test
    fun `getClientIdFromToken when claims do not have clientId`() {
        given(claims["CLIENT_ID"]).willReturn(null)
        assertNull(refreshTokenHandler.getClientIdFromToken(refreshTokenStr, parser))
    }

    @Test
    fun `getClientIdFromToken when claims have clientId`() {
        given(claims["CLIENT_ID"]).willReturn(clientId)
        assertEquals(clientId, refreshTokenHandler.getClientIdFromToken(refreshTokenStr, parser))
    }

    @Test
    fun `isValidToken should return false if passed token is expired`() {
        // Stub
        given(claims.expiration).willReturn(Date.from(now.minus(10, ChronoUnit.MINUTES)))
        given(claims["USER_ID"]).willReturn(userId)
        given(claims["CLIENT_ID"]).willReturn(clientId)

        // Call method and assert
        assertFalse(refreshTokenHandler.isValidToken(refreshTokenStr, parser, now))
    }

    @Test
    fun `isValidToken should return false if passed token does not have userId`() {
        // Stub
        given(claims.expiration).willReturn(Date.from(now.plus(10, ChronoUnit.MINUTES)))
        given(claims["USER_ID"]).willReturn(null)
        given(claims["CLIENT_ID"]).willReturn(clientId)

        // Call method and assert
        assertFalse(refreshTokenHandler.isValidToken(refreshTokenStr, parser, now))
    }

    @Test
    fun `isValidToken should return false if passed token does not have clientId`() {
        // Stub
        given(claims.expiration).willReturn(Date.from(now.plus(10, ChronoUnit.MINUTES)))
        given(claims["USER_ID"]).willReturn(userId)
        given(claims["CLIENT_ID"]).willReturn(null)

        // Call method and assert
        assertFalse(refreshTokenHandler.isValidToken(refreshTokenStr, parser, now))
    }

    @Test
    fun `isValidToken should return true if valid token`() {
        // Stub
        given(claims.expiration).willReturn(Date.from(now.plus(10, ChronoUnit.MINUTES)))
        given(claims["USER_ID"]).willReturn(userId)
        given(claims["CLIENT_ID"]).willReturn(clientId)

        // Call method and assert
        assertTrue(refreshTokenHandler.isValidToken(refreshTokenStr, parser, now))
    }

}