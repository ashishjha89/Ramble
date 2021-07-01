package com.ramble.identity.controllers

import com.ramble.identity.common.*
import com.ramble.identity.models.RegisterUserRequest
import com.ramble.identity.service.UserService
import com.ramble.identity.utils.toResponseEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(USER_INFO_API_BASE_PATH)
class IdentityController(private val userService: UserService) {

    @GetMapping(USER_INFO_GREETING_PATH)
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String) =
            "Hello $name"

    @PostMapping(USER_INFO_REGISTER_PATH)
    fun signUp(@RequestBody user: RegisterUserRequest): ResponseEntity<*> {
        println("IdentityController signUp() user:$user")
        return userService.saveUser(email = user.email, password = user.password).toResponseEntity()
    }

    @GetMapping(USER_INFO_ME_PATH)
    fun getMyInfo(@RequestHeader headers: Map<String, String>): ResponseEntity<*> {
        println("IdentityController getMyInfo()")
        return userService.getUserInfoResult(token = headers[AUTHORIZATION_HEADER]).toResponseEntity()
    }

}
