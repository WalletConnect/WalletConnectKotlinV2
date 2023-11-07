#!/usr/bin/env kotlin

@file:DependsOn("io.github.typesafegithub:github-workflows-kt:1.4.0")

import io.github.typesafegithub.workflows.actions.actions.CheckoutV4
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.writeToFile
import kotlin.io.path.pathString

workflow(
    name = "Test workflow",
    on = listOf(WorkflowDispatch(emptyMap(), emptyMap())),
    sourceFile = __FILE__.toPath().also { println(it.pathString) },
    yamlConsistencyJobCondition = null
) {
    job(id = "test_job", runsOn = UbuntuLatest, needs = emptyList()) {
        uses(
            name = "Check out",
            action = CheckoutV4(ref = "feature/meta/workflow_to_notify_of_new_release")
        )
        run(name = "Print greeting", command = "echo 'Hello world!'")
        run(name = "Print greeting", command = "echo 'Hello world!2'")
    }
}.writeToFile(addConsistencyCheck = false)