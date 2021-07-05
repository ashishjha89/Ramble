package com.ramble.identity.configurations

import com.google.gson.Gson
import com.ramble.identity.common.unauthorizedAccess
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AuthenticationEntryPointInterceptor : AuthenticationEntryPoint {

    override fun commence(request: HttpServletRequest?, response: HttpServletResponse?, authException: AuthenticationException?) {
        response ?: return
        response.apply {
            contentType = "application/json"
            characterEncoding = "UTF-8"
            status = HttpServletResponse.SC_UNAUTHORIZED
        }
        response.writer.apply {
            print(Gson().toJson(unauthorizedAccess))
            flush()
        }
    }

}