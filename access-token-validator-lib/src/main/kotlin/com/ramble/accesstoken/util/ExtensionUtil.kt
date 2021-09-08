package com.ramble.accesstoken.util

import java.util.*

val <T> Optional<T>.value: T?
    get() = orElse(null)