package com.walletconnect.notify.engine.domain

fun createAuthorizationReCaps(): String {
    // {"att":{"https://notify.walletconnect.com":{"manage/all-apps-notifications":[{}]}}}
    return "urn:recap:eyJhdHQiOnsiaHR0cHM6Ly9ub3RpZnkud2FsbGV0Y29ubmVjdC5jb20iOnsibWFuYWdlL2FsbC1hcHBzLW5vdGlmaWNhdGlvbnMiOlt7fV19fX0"
}