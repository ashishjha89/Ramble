package com.ramble.identity.controller

import javax.servlet.http.HttpServletResponse

const val BAD_REQUEST = HttpServletResponse.SC_BAD_REQUEST.toString()
const val FORBIDDEN = HttpServletResponse.SC_FORBIDDEN.toString()
const val INTERNAL_SERVER_ERROR = HttpServletResponse.SC_INTERNAL_SERVER_ERROR.toString()
const val OK = HttpServletResponse.SC_OK.toString()