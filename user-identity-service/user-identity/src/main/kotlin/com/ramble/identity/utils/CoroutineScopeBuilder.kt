package com.ramble.identity.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.springframework.stereotype.Component

@Component
class CoroutineScopeBuilder {

    val defaultIoScope: CoroutineScope
        get() = CoroutineScope(Job() + Dispatchers.IO)
}