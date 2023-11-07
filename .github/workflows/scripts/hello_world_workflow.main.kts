#!/usr/bin/env kotlin

@file:DependsOn("io.github.typesafegithub:github-workflows-kt:1.4.0")

import io.github.typesafegithub.workflows.actions.actions.CheckoutV4
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.actions.CustomAction
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.expressions.Contexts
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.writeToFile
import org.intellij.lang.annotations.Language

@Suppress("PrivatePropertyName")
private val SLACK_WEBHOOK_URL by Contexts.secrets

@Language("JSON")
private val slackPayload = """
                            {
                              "text":"Hello, World!"
                            }
                            """.trimIndent()

workflow(
    name = "Test workflow",
    on = listOf(WorkflowDispatch()),
    sourceFile = __FILE__.toPath(),
) {
    job(id = "test_job", runsOn = UbuntuLatest) {
        uses(
            name = "Check out",
            action = CheckoutV4(ref = "feature/meta/workflow_to_notify_of_new_release")
        )
        run(
            name = "Print greeting",
            command = "echo 'Hello world!' ${expr { SLACK_WEBHOOK_URL }} after secret"
        )
        uses(
            name = "Notify Channel",
            action = CustomAction(
                actionOwner = "slackapi",
                actionName = "slack-github-action",
                actionVersion = "v1.24.0",
                inputs = linkedMapOf(
                    "payload" to slackPayload
                )
            ),
            env = linkedMapOf(
                "SLACK_WEBHOOK_URL" to expr { SLACK_WEBHOOK_URL },
                "SLACK_WEBHOOK_TYPE" to "INCOMING_WEBHOOK"
            )
        )
    }
}.writeToFile(addConsistencyCheck = false)