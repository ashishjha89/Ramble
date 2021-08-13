package com.ramble.token.handler

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

class RegistrationConfirmationTokenHandlerTest {

    private val jwtKey = mock(SecretKey::class.java)
    private val tokenDurationGenerator = mock(TokenDurationGenerator::class.java)
    private val emailId = "someEmailId@random.com"
    private val userId = "someUserId"

    private val claims = mock(Claims::class.java)
    private val registrationConfirmationTokenStr = "some_registration_confirmation_token"
    private val parser = mock(JwtParser::class.java)
    private val now = Instant.now()

    private val registrationConfirmationTokenHandler
            by lazy { RegistrationConfirmationTokenHandler(jwtKey, tokenDurationGenerator) }

    @Suppress("unchecked_cast")
    @Before
    fun setup() {
        val jwsClaims = mock(Jws::class.java)
        given(parser.parseClaimsJws(registrationConfirmationTokenStr)).willReturn(jwsClaims as Jws<Claims>)
        given(jwsClaims.body).willReturn(claims)
    }

    @Test
    fun generateAccessTokenTest() {
        val jwtBuilder = mock(JwtBuilder::class.java)
        val issuedInstant = Instant.now()
        val expiryDurationAmount = 30L
        val expiryDurationUnit = ChronoUnit.MINUTES
        val tokenDuration = TokenDuration(
            issuedDate = Date.from(issuedInstant),
            expiryDate = Date.from(issuedInstant.plus(expiryDurationAmount, expiryDurationUnit))
        )
        val claimsMap = mapOf("USER_ID" to userId)
        val registrationTokenSigned = "some_signed_registration_token"

        // Stub
        given(tokenDurationGenerator.getTokenDuration(issuedInstant, expiryDurationAmount, expiryDurationUnit))
            .willReturn(tokenDuration)
        given(jwtBuilder.setClaims(claimsMap)).willReturn(jwtBuilder)
        given(jwtBuilder.setSubject(emailId)).willReturn(jwtBuilder)
        given(jwtBuilder.setIssuedAt(tokenDuration.issuedDate)).willReturn(jwtBuilder)
        given(jwtBuilder.setExpiration(tokenDuration.expiryDate)).willReturn(jwtBuilder)
        given(jwtBuilder.signWith(jwtKey, SignatureAlgorithm.HS512)).willReturn(jwtBuilder)
        given(jwtBuilder.compact()).willReturn(registrationTokenSigned)

        // Call method and assert
        assertEquals(
            registrationTokenSigned,
            registrationConfirmationTokenHandler.generateToken(
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
    fun `isValidToken should return false if passed token is expired`() {
        val expiredInstant = now.minus(10, ChronoUnit.MINUTES)

        // Stub
        given(claims.expiration).willReturn(Date.from(expiredInstant))

        // Call method and assert
        assertFalse(registrationConfirmationTokenHandler.isValidToken(registrationConfirmationTokenStr, now, parser))
    }

    @Test
    fun `isValidToken should return false if passed token does not have userId`() {
        val expiredInstant = now.plus(10, ChronoUnit.MINUTES)

        // Stub
        given(claims.expiration).willReturn(Date.from(expiredInstant))
        given(claims.subject).willReturn(emailId)
        given(claims["USER_ID"]).willReturn(null)

        // Call method and assert
        assertFalse(registrationConfirmationTokenHandler.isValidToken(registrationConfirmationTokenStr, now, parser))
    }

    @Test
    fun `isValidToken should return true if passed token is valid`() {
        val expiredInstant = now.plus(10, ChronoUnit.MINUTES)

        // Stub
        given(claims.expiration).willReturn(Date.from(expiredInstant))
        given(claims.subject).willReturn(emailId)
        given(claims["USER_ID"]).willReturn(userId)

        // Call method and assert
        assertTrue(registrationConfirmationTokenHandler.isValidToken(registrationConfirmationTokenStr, now, parser))
    }

    @Test
    fun `getUserId return null if userId is missing in token`() {
        // Stub
        given(claims["USER_ID"]).willReturn(null)

        // Call method and assert
        assertNull(registrationConfirmationTokenHandler.getUserIdFromToken(registrationConfirmationTokenStr, parser))
    }

    @Test
    fun `getUserId return user if userId is present in token`() {
        // Stub
        given(claims["USER_ID"]).willReturn(userId)

        // Call method and assert
        assertEquals(
            userId,
            registrationConfirmationTokenHandler.getUserIdFromToken(registrationConfirmationTokenStr, parser)
        )
    }

}