package com.walletconnect.sync.engine.use_case

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


// todo: discuss if creating a separate for each scope use case is a good idea. If yes, then we might want to move this class to android core, for future refactor of other apis
internal abstract class SuspendUseCase {
    protected var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}