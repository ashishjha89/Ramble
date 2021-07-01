package com.ramble.identity.configurations

import com.google.gson.Gson
import com.ramble.identity.common.ErrorBody
import com.ramble.identity.common.ErrorCode
import com.ramble.identity.common.ErrorMessage
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AuthenticationEntryPointImpl : AuthenticationEntryPoint {

    override fun commence(request: HttpServletRequest?, response: HttpServletResponse?, authException: AuthenticationException?) {
        response ?: return
        response.apply {
            contentType = "application/json"
            characterEncoding = "UTF-8"
            status = HttpServletResponse.SC_UNAUTHORIZED
        }
        val error = ErrorBody(errorCode = ErrorCode.UNAUTHORIZED_ACCESS, errorMessage = ErrorMessage.unauthorizedAccess)
        response.writer.apply {
            print(Gson().toJson(error))
            flush()
        }
    }

}