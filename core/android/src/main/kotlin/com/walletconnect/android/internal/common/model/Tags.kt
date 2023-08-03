package com.walletconnect.android.internal.common.model

enum class Tags(val id: Int) {
    UNSUPPORTED_METHOD(0),

    PAIRING_DELETE(1000),
    PAIRING_DELETE_RESPONSE(1001),

    PAIRING_PING(1002),
    PAIRING_PING_RESPONSE(1003),

    SESSION_PROPOSE(1100),
    SESSION_PROPOSE_RESPONSE(1101),

    SESSION_SETTLE(1102),
    SESSION_SETTLE_RESPONSE(1103),

    SESSION_UPDATE(1104),
    SESSION_UPDATE_RESPONSE(1105),

    SESSION_EXTEND(1106),
    SESSION_EXTEND_RESPONSE(1107),

    SESSION_REQUEST(1108),
    SESSION_REQUEST_RESPONSE(1109),

    SESSION_EVENT(1110),
    SESSION_EVENT_RESPONSE(1111),

    SESSION_DELETE(1112),
    SESSION_DELETE_RESPONSE(1113),

    SESSION_PING(1114),
    SESSION_PING_RESPONSE(1115),

    CHAT_INVITE(2000),
    CHAT_INVITE_RESPONSE(2001),

    CHAT_MESSAGE(2002),
    CHAT_MESSAGE_RESPONSE(2003),

    CHAT_LEAVE(2004),
    CHAT_LEAVE_RESPONSE(2005),

    CHAT_PING(2006),
    CHAT_PING_RESPONSE(2007),

    AUTH_REQUEST(3000),
    AUTH_REQUEST_RESPONSE(3001),

    @Deprecated("Replaced with notify protocol")
    PUSH_REQUEST(4000),

    @Deprecated("Replaced with notify protocol")
    PUSH_REQUEST_RESPONSE(4001),
    NOTIFY_SUBSCRIBE(4000),
    NOTIFY_SUBSCRIBE_RESPONSE(4001),

    @Deprecated("Replaced with notify protocol")
    PUSH_PROPOSE(4010),

    @Deprecated("Replaced with notify protocol")
    PUSH_PROPOSE_RESPONSE(4011),

    @Deprecated("Replaced with notify protocol")
    PUSH_MESSAGE(4002),

    @Deprecated("Replaced with notify protocol")
    PUSH_MESSAGE_RESPONSE(4003),
    NOTIFY_MESSAGE(4002),
    NOTIFY_MESSAGE_RESPONSE(4003),

    @Deprecated("Replaced with notify protocol")
    PUSH_DELETE(4004),
    @Deprecated("Replaced with notify protocol")
    PUSH_DELETE_RESPONSE(4005),
    NOTIFY_DELETE(4004),
    NOTIFY_DELETE_RESPONSE(4005),

    @Deprecated("Replaced with notify protocol")
    PUSH_SUBSCRIBE(4006),
    @Deprecated("Replaced with notify protocol")
    PUSH_SUBSCRIBE_RESPONSE(4007),

    @Deprecated("Replaced with notify protocol")
    PUSH_UPDATE(4008),
    @Deprecated("Replaced with notify protocol")
    PUSH_UPDATE_RESPONSE(4009),
    NOTIFY_UPDATE(4008),
    NOTIFY_UPDATE_RESPONSE(4009),

    SYNC_SET(5000),

    SYNC_DELETE(5002),

    //todo: Discuss: Should Tags be in specific SDKs?
}