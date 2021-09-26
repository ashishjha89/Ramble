package com.ramble.messaging.repo

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

@Component
class UserApiComponent {

    fun getHttpEntity(accessToken: String): HttpEntity<String> {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(accessToken)
        }
        return HttpEntity<String>(headers)
    }

    fun getUserProfileApiUrl(userId: String): String =
        "http://user-identity-api/user-identity/user-info/v1/user/$userId"

}