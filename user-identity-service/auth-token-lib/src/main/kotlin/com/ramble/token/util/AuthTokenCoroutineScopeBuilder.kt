package com.ramble.token.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.springframework.stereotype.Component

@Component
class AuthTokenCoroutineScopeBuilder {

    val defaultIoScope: CoroutineScope
        get() = CoroutineScope(Job() + Dispatchers.IO)
}