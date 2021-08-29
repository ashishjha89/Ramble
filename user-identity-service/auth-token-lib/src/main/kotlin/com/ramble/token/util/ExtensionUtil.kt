package com.ramble.token.util

import java.util.*

val <T> Optional<T>.value: T?
    get() = orElse(null)