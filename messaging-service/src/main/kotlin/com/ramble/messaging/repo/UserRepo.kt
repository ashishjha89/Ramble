package com.ramble.messaging.repo

import com.ramble.messaging.model.InternalServerException
import com.ramble.messaging.model.UserNotFoundException
import com.ramble.messaging.model.UserProfile
import com.ramble.messaging.utils.MessagingCoroutineScopeBuilder
import com.ramble.messaging.utils.performDeferredTask
import kotlinx.coroutines.async
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate

@Repository
class UserRepo(
    private val restTemplate: RestTemplate,
    private val coroutineScopeBuilder: MessagingCoroutineScopeBuilder
) {

    companion object {
        private const val IDENTITY_API_TIMEOUT = 500L
    }

    @Throws(UserNotFoundException::class, InternalServerException::class)
    suspend fun getUserProfile(
        emailId: String,
        accessToken: String,
        timeoutInMilliseconds: Long = IDENTITY_API_TIMEOUT
    ): UserProfile {
        val scope = coroutineScopeBuilder.defaultIoScope
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(accessToken)
        }
        val entity = HttpEntity<String>(headers)
        val userProfileApiUrl = "http://user-identity-api/user-identity/user-info/v1/user/$emailId"
        return performDeferredTask(
            deferredTask = scope.async {
                restTemplate.exchange(
                    userProfileApiUrl,
                    HttpMethod.GET,
                    entity,
                    UserProfile::class.java
                )
            },
            timeoutInMilliseconds = timeoutInMilliseconds
        ).body ?: throw UserNotFoundException()
    }
}