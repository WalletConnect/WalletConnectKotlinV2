#!/usr/bin/env kotlin
@file:Suppress("PrivatePropertyName")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:1.4.0")

import io.github.typesafegithub.workflows.actions.actions.CheckoutV4
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.actions.Action
import io.github.typesafegithub.workflows.domain.actions.RegularAction
import io.github.typesafegithub.workflows.domain.triggers.Release
import io.github.typesafegithub.workflows.dsl.expressions.Contexts
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.writeToFile
import org.intellij.lang.annotations.Language

private val SLACK_WEBHOOK_URL_KOTLIN by Contexts.secrets

workflow(
    name = "Slack Release Notifications",
    on = listOf(
        Release(_customArguments = mapOf("types" to listOf("published")))
    ),
    sourceFile = __FILE__.toPath(),
) {
    job(
        id = "push-notifications",
        runsOn = UbuntuLatest,
        _customArguments = mapOf(
            "strategy" to mapOf(
                "matrix" to mapOf(
                    "conf" to listOf(
                        mapOf("webhook_url" to expr { SLACK_WEBHOOK_URL_KOTLIN }),
                    )
                )
            )
        )
    ) {
        uses(
            name = "Check out",
            action = CheckoutV4()
        )
        uses(
            name = "Notify Channel",
            action = SlackGithubAction(),
            env = linkedMapOf(
                "SLACK_WEBHOOK_URL" to expr { "matrix.conf.webhook_url" },
                "SLACK_WEBHOOK_TYPE" to "INCOMING_WEBHOOK"
            )
        )
    }
}.writeToFile(addConsistencyCheck = false)

private class SlackGithubAction : RegularAction<Action.Outputs>(
    actionOwner = "slackapi",
    actionName = "slack-github-action",
    actionVersion = "v1.24.0"
) {
    @Language("JSON")
    private val slackPayload =
        """
        {
          "text":"Kotlin Release ${expr { github.eventRelease.release.name }} was just released. Please update and let us know if you have any issues."
        }
        """.trimIndent()

    override fun buildOutputObject(stepId: String): Outputs = Outputs(stepId)

    override fun toYamlArguments(): LinkedHashMap<String, String> =
        linkedMapOf(
            "payload" to slackPayload
        )
}