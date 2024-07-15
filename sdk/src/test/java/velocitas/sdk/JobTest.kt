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

package velocitas.sdk

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlin.concurrent.thread

class JobTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerTest

    given("A short running Job") {
        var isExecuted = false
        val job = Job { isExecuted = true }
        `when`("Checking it's recur state") {
            val recurState = job.shallRecur()

            then("It should return false") {
                recurState shouldBe false
            }
        }
        then("The passed function is not called automatically") {
            isExecuted shouldBe false
        }

        `when`("execute is called") {
            job.execute()

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
            job.execute()
        }

        `when`("Waiting for termination") {
            job.waitForTermination()

            then("It should not return while executing") {
                isExecuting shouldBe false
            }
        }
    }

    given("A short running RecurringJob") {
        var isExecuted = false
        val recurringJob = RecurringJob { isExecuted = true }
        `when`("Checking the state of shallRecur") {
            val shallRecur = recurringJob.shallRecur()

            then("It should return true") {
                shallRecur shouldBe true
            }
        }

        then("The passed function is not called automatically") {
            isExecuted shouldBe false
        }

        `when`("execute is called") {
            recurringJob.execute()

            then("The passed function is executed") {
                isExecuted shouldBe true
            }
        }

        `when`("Cancel is called") {
            val result = runCatching {
                recurringJob.cancel()
            }
            then("No Exception should be thrown") {
                result.isFailure shouldBe false
                result.exceptionOrNull() shouldBe null
            }
            then("shallRecur should return false") {
                recurringJob.shallRecur() shouldBe false
            }

            and("When the job is executed afterwards") {
                recurringJob.execute()

                then("The passed function should not be executed") {
                    isExecuted shouldBe false
                }
            }
        }
    }
})
