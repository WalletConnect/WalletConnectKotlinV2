package com.walletconnect.chat.core.model.vo

enum class Tags(val id: Int) {
    CHAT_INVITE(2000),
    CHAT_INVITE_RESPONSE(2001),

    CHAT_MESSAGE(2002),
    CHAT_MESSAGE_RESPONSE(2003),

    CHAT_LEAVE(2004),
    CHAT_LEAVE_RESPONSE(2005),

    CHAT_PING(2006),
    CHAT_PING_RESPONSE(2007),
}