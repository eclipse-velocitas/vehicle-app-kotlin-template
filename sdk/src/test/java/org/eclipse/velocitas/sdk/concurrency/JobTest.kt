/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.velocitas.sdk.concurrency

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class JobTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerTest

    given("A short running Job") {
        var isExecuted = false
        val job = Job { isExecuted = true }
        `when`("Checking it's recur state") {
            val recurState = job.shallRecur

            then("It should return false") {
                recurState shouldBe false
            }
        }
        then("The passed function is not called automatically") {
            isExecuted shouldBe false
        }

        `when`("execute is called") {
            job.run()

            then("The passed function is executed") {
                isExecuted shouldBe true
            }
        }

        `when`("The job is waiting for termination") {
            val result = runCatching {
                job.waitForTermination()
            }

            then("No Exception is thrown and the method is returned immediately") {
                result.isFailure shouldBe false
                result.exceptionOrNull() shouldBe null
            }
        }
    }

    given("A long running job") {
        var isExecuting: Boolean? = null
        val job = Job {
            isExecuting = true
            Thread.sleep(500)
            isExecuting = false
        }

        thread {
            job.run()
        }

        `when`("Waiting for termination") {
            job.waitForTermination()

            then("It should not return while executing") {
                isExecuting shouldBe false
            }
        }
    }

    given("A short running RecurringJob") {
        val recurringOptions = RecurringOptions(0L, 100L, TimeUnit.MILLISECONDS)

        var isExecuted = false
        val recurringJob = RecurringJob(recurringOptions) { isExecuted = true }
        `when`("Checking the state of shallRecur") {
            val shallRecur = recurringJob.shallRecur

            then("It should return true") {
                shallRecur shouldBe true
            }
        }

        then("The passed function is not called automatically") {
            isExecuted shouldBe false
        }

        `when`("execute is called") {
            recurringJob.run()

            then("The passed function is executed") {
                isExecuted shouldBe true
            }
        }
    }
})
