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
