package com.walletconnect.sign.mappers

import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SessionProposer
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.engine.model.mapper.toEngineDO
import org.junit.Test

class EngineMapperTest {

    @Test
    fun testIfURISyntaxExceptionIsCaught() {
        val svgIcon = """
            <?xml version="1.0" encoding="utf-8"?>
            <!-- Generator: Adobe Illustrator 23.0.3, SVG Export Plug-In . SVG Version: 6.00 Build 0)  -->
            <svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
            	 viewBox="0 0 256 256" style="enable-background:new 0 0 256 256;" xml:space="preserve">
            <style type="text/css">
            	.st1 {
            		fill: #130E25;
            	}
            </style>
            <g>		<g id="f3c79a17-a9a5-4491-b2e1-a7a08003ed20_1_" transform="matrix(0.5053180100095477,0,0,0.5053180100095477,40,129.49267910725604)">
            		<radialGradient id="SVGID_1_" cx="174.1478" cy="-7.1596" r="107.5926" gradientUnits="userSpaceOnUse">
            			<stop  offset="0" style="stop-color:#8F4AFF"/>
            			<stop  offset="1" style="stop-color:#5E00CA"/>
            		</radialGradient>
            		<path class="st1" d="M347.36-196.92H0.94L-59.65-86.77l233.8,269.37l233.8-269.37L347.36-196.92z M331.42-167.25l35.49,64.53
            			h-63.78C303.13-102.72,331.42-167.25,331.42-167.25z M301.18-168.67l-25.89,59.08l-64.71-59.08H301.18z M240.89-102.72H107.41
            			l66.74-60.94L240.89-102.72z M137.71-168.67l-64.71,59.08l-25.9-59.08H137.71z M16.87-167.25l28.29,64.53h-63.78L16.87-167.25z
            			 M-11.54-74.46h69.09l70.5,160.85L-11.54-74.46z M88.41-74.46h171.49l-85.75,195.61L88.41-74.46z M220.24,86.39l70.5-160.85h69.1
            			L220.24,86.39z"/>
            	</g>
            </g>
            </svg>
        """.trimIndent()

        val sessionProposeParams = SignParams.SessionProposeParams(
            requiredNamespaces = emptyMap(),
            optionalNamespaces = null,
            relays = listOf(RelayProtocolOptions()),
            properties = emptyMap(),
            proposer = SessionProposer("", AppMetaData("", "", listOf(svgIcon), "")),
        )

        sessionProposeParams.toEngineDO(Topic("topic")).also {
            assert(it.icons.isEmpty())
        }
    }
}