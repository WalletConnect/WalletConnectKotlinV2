package com.walletconnect.android.test.json_rpc

import com.walletconnect.android.internal.common.model.type.JsonRpcClientSync
import com.walletconnect.util.generateId

data class TestHistoryRPC(
    override val id: Long = generateId(),
    override val method: String = JsonRpcMethod.TEST_HISTORY,
    override val jsonrpc: String = "2.0",
    override val params: TestHistoryParams,
) : JsonRpcClientSync<TestHistoryParams>
