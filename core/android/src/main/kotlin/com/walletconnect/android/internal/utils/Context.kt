package com.walletconnect.android.internal.utils

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

private const val SELF_PARTICIPANT_CONTEXT = "self_participant/"
private const val SELF_INVITE_PUBLIC_KEY_CONTEXT = "self_inviteKey/"
private const val SELF_IDENTITY_PUBLIC_KEY_CONTEXT = "self_identityKey/"

fun AccountId.getInviteTag(): String = "$SELF_INVITE_PUBLIC_KEY_CONTEXT${this.value}"
fun Topic.getParticipantTag(): String = "$SELF_PARTICIPANT_CONTEXT${this.value}"
fun AccountId.getIdentityTag(): String = "$SELF_IDENTITY_PUBLIC_KEY_CONTEXT${this.value}"