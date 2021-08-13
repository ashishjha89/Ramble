package com.ramble.identity.utils

import com.ramble.identity.common.BEARER

fun getTokenFromBearerHeader(bearerStr: String): String? {
    if (!bearerStr.startsWith(BEARER)) return null
    return bearerStr.substring(BEARER.length)
}
