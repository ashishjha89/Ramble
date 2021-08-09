package com.ramble.token

import java.util.*

val <T> Optional<T>.value: T?
    get() = orElse(null)