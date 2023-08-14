package com.walletconnect.sign.engine.use_case

import com.walletconnect.sign.common.model.vo.proposal.ProposalVO
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDO
import com.walletconnect.sign.storage.proposal.ProposalStorageRepository

internal class GetSessionProposalsUseCase(private val proposalStorageRepository: ProposalStorageRepository) : GetSessionProposalsUseCaseInterface {
    override fun getSessionProposals(): List<EngineDO.SessionProposal> = proposalStorageRepository.getProposals().map(ProposalVO::toEngineDO)
}

internal interface GetSessionProposalsUseCaseInterface {
    fun getSessionProposals(): List<EngineDO.SessionProposal>
}