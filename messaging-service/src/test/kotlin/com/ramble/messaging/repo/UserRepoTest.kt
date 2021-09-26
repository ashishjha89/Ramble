package com.ramble.messaging.repo

import com.ramble.messaging.common.userInfoNotFound
import com.ramble.messaging.model.InternalServerException
import com.ramble.messaging.model.UserNotFoundException
import com.ramble.messaging.model.UserProfile
import com.ramble.messaging.utils.MessagingCoroutineScopeBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.http.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals

class UserRepoTest {

    private val restTemplate = mock(RestTemplate::class.java)
    private val userApiComponent = mock(UserApiComponent::class.java)
    private val coroutineScopeBuilder = mock(MessagingCoroutineScopeBuilder::class.java)
    private val scope = CoroutineScope(Job())

    private val userId = "someUserId"
    private val email = "someEmailId"
    private val accessToken = "someAccessToken"

    private val userRepo = UserRepo(restTemplate, userApiComponent, coroutineScopeBuilder)

    @Before
    fun setup() {
        given(coroutineScopeBuilder.defaultIoScope).willReturn(scope)
    }

    @Test
    fun `getUserProfile when api returns back user`() = runBlocking {
        val userProfile = UserProfile(id = userId, email = email)
        val expectedResponseEntity = ResponseEntity<UserProfile>(userProfile, HttpStatus.OK)

        val expectedHttpEntity = httpEntity()
        val expectedUserProfileApiUrl = userProfileApiUrl()

        given(userApiComponent.getUserProfileApiUrl(userId)).willReturn(expectedUserProfileApiUrl)
        given(userApiComponent.getHttpEntity(accessToken)).willReturn(expectedHttpEntity)
        given(
            restTemplate.exchange(
                expectedUserProfileApiUrl, HttpMethod.GET, expectedHttpEntity, UserProfile::class.java
            )
        ).willReturn(expectedResponseEntity)

        // Call method and assert
        assertEquals(userProfile, userRepo.getUserProfile(userId, accessToken))
    }

    @Test(expected = InternalServerException::class)
    fun `getUserProfile should throw InternalServerException when api returns null body`() = runBlocking<Unit> {
        val expectedResponseEntity = ResponseEntity<UserProfile>(null, HttpStatus.OK)

        val expectedHttpEntity = httpEntity()
        val expectedUserProfileApiUrl = userProfileApiUrl()

        given(userApiComponent.getUserProfileApiUrl(userId)).willReturn(expectedUserProfileApiUrl)
        given(userApiComponent.getHttpEntity(accessToken)).willReturn(expectedHttpEntity)
        given(
            restTemplate.exchange(
                expectedUserProfileApiUrl, HttpMethod.GET, expectedHttpEntity, UserProfile::class.java
            )
        ).willReturn(expectedResponseEntity)

        // Call method
        userRepo.getUserProfile(userId, accessToken)
    }

    @Test(expected = UserNotFoundException::class)
    fun `getUserProfile should throw UserNotFoundException when api returns HttpClientErrorException and User_Not_Found error code`() =
        runBlocking<Unit> {
            val errorBodyStr =
                """ 
                    { 
                        "errorCode": "${userInfoNotFound.errorCode}", 
                        "errorMessage":"${userInfoNotFound.errorMessage}" 
                    } 
                    """.trimIndent()

            val errorBody = errorBodyStr.toByteArray(StandardCharsets.UTF_8)
            val expectedException = HttpClientErrorException(
                HttpStatus.NOT_FOUND, "", errorBody, StandardCharsets.UTF_8
            )
            val expectedHttpEntity = httpEntity()
            val expectedUserProfileApiUrl = userProfileApiUrl()

            given(userApiComponent.getUserProfileApiUrl(userId)).willReturn(expectedUserProfileApiUrl)
            given(userApiComponent.getHttpEntity(accessToken)).willReturn(expectedHttpEntity)
            given(
                restTemplate.exchange(
                    expectedUserProfileApiUrl, HttpMethod.GET, expectedHttpEntity, UserProfile::class.java
                )
            ).willThrow(expectedException)

            // Call method
            userRepo.getUserProfile(userId, accessToken)
        }

    @Test(expected = InternalServerException::class)
    fun `getUserProfile should throw InternalServerException when api returns HttpClientErrorException but unrecognized error-code`() =
        runBlocking<Unit> {
            val errorBodyRandomFormatStr =
                """ 
                    { 
                        "errorCode": "RANDOM_ERROR_CODE", 
                        "errorMessage":"${userInfoNotFound.errorMessage}" 
                    } 
                    """.trimIndent()

            val errorBodyRandomFormat = errorBodyRandomFormatStr.toByteArray(StandardCharsets.UTF_8)
            val expectedException = HttpClientErrorException(
                HttpStatus.NOT_FOUND, "", errorBodyRandomFormat, StandardCharsets.UTF_8
            )
            val expectedHttpEntity = httpEntity()
            val expectedUserProfileApiUrl = userProfileApiUrl()

            given(userApiComponent.getUserProfileApiUrl(userId)).willReturn(expectedUserProfileApiUrl)
            given(userApiComponent.getHttpEntity(accessToken)).willReturn(expectedHttpEntity)
            given(
                restTemplate.exchange(
                    expectedUserProfileApiUrl, HttpMethod.GET, expectedHttpEntity, UserProfile::class.java
                )
            ).willThrow(expectedException)

            // Call method
            userRepo.getUserProfile(userId, accessToken)
        }

    @Test(expected = InternalServerException::class)
    fun `getUserProfile should throw InternalServerException when api returns HttpClientErrorException but null body object`() =
        runBlocking<Unit> {
            val expectedException = HttpClientErrorException(
                HttpStatus.NOT_FOUND, "", null, StandardCharsets.UTF_8
            )
            val expectedHttpEntity = httpEntity()
            val expectedUserProfileApiUrl = userProfileApiUrl()

            given(userApiComponent.getUserProfileApiUrl(userId)).willReturn(expectedUserProfileApiUrl)
            given(userApiComponent.getHttpEntity(accessToken)).willReturn(expectedHttpEntity)
            given(
                restTemplate.exchange(
                    expectedUserProfileApiUrl, HttpMethod.GET, expectedHttpEntity, UserProfile::class.java
                )
            ).willThrow(expectedException)

            // Call method
            userRepo.getUserProfile(userId, accessToken)
        }

    @Test(expected = RestClientException::class)
    fun `getUserProfile should throw RestClientException when api returns RestClientException`() =
        runBlocking<Unit> {
            val expectedHttpEntity = httpEntity()
            val expectedUserProfileApiUrl = userProfileApiUrl()

            given(userApiComponent.getUserProfileApiUrl(userId)).willReturn(expectedUserProfileApiUrl)
            given(userApiComponent.getHttpEntity(accessToken)).willReturn(expectedHttpEntity)
            given(
                restTemplate.exchange(
                    expectedUserProfileApiUrl, HttpMethod.GET, expectedHttpEntity, UserProfile::class.java
                )
            ).willThrow(RestClientException("Random exception"))

            // Call method
            userRepo.getUserProfile(userId, accessToken)
        }

    private fun httpEntity(): HttpEntity<String> {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(accessToken)
        }
        return HttpEntity<String>(headers)
    }

    private fun userProfileApiUrl(): String =
        "http://user-identity-api/user-identity/user-info/v1/user/$userId"
}