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

import io.kotest.assertions.nondeterministic.continually
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.names.DuplicateTestNameMode
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import java.util.LinkedList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val testRecurringOptions = RecurringOptions(0L, 100L, TimeUnit.MILLISECONDS)

class ThreadPoolTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerTest
    duplicateTestNameMode = DuplicateTestNameMode.Silent

    val fakeJobs = LinkedList<FakeJob>()
    val classUnderTest = ThreadPool()

    val defaultTimeout = 100L
    val defaultTimeUnit = TimeUnit.MILLISECONDS

    fun createAndExecuteJob(timeout: Long = defaultTimeout, timeUnit: TimeUnit = defaultTimeUnit): Boolean {
        val fakeJob = FakeJob()
        fakeJobs.add(fakeJob)
        classUnderTest.enqueue(fakeJob)

        return fakeJob.waitForExecution(timeout, timeUnit)
    }

    fun finishJob(job: FakeJob, timeout: Long = defaultTimeout, timeUnit: TimeUnit = defaultTimeUnit): Boolean {
        job.finish()
        return job.waitForFinished(timeout, timeUnit)
    }

    fun stopCreatedJobs() {
        while (fakeJobs.isNotEmpty()) {
            val fakeJob = fakeJobs.remove()
            fakeJob.finish()
        }
    }

    fun occupyAllWorkers(timeout: Long = defaultTimeout, timeUnit: TimeUnit = defaultTimeUnit): Boolean {
        val times = classUnderTest.numWorkerThreads
        repeat(times) {
            if (!createAndExecuteJob(timeout, timeUnit)) {
                return false
            }
        }
        return true
    }

    context("Enqueueing a job") {
        `when`("A RecurringJob is enqueued") {
            var counter = 0
            val recurringJob = RecurringJob(testRecurringOptions) {
                counter++
            }

            classUnderTest.enqueue(recurringJob)

            then("It should be executed multiple times") {
                eventually(1.seconds) {
                    counter shouldBeGreaterThan 1
                }
            }

            and("After cancellation of the task") {
                classUnderTest.cancelRecurringJob(recurringJob)

                then("It should no longer be executed") {
                    val currentCounter = counter

                    continually(50.milliseconds) {
                        counter shouldBe currentCounter
                    }
                }
            }
        }

        `when`("A non-recurring Job is enqueued") {
            var counter = 0
            val nonRecurringJob = Job {
                counter++
            }

            classUnderTest.enqueue(nonRecurringJob)

            then("It should be executed exactly once") {
                continually(50.milliseconds) {
                    counter shouldBe 1
                }
            }
        }
    }

    given("A FakeJob") {
        val job = FakeJob()

        `when`("It is initialized") {
            then("It should be in Initialized state") {
                job.atomicExecutionState.get() shouldBe State.Initialized
            }
        }

        `when`("It is enqueued") {
            classUnderTest.enqueue(job)
            job.waitForExecution()

            then("It should enter the executing state") {
                job.atomicExecutionState.get() shouldBe State.Executing
            }

            and("When it is finished") {
                finishJob(job)

                then("It should not be executed again") {
                    job.waitForExecution() shouldBe false
                    job.atomicExecutionCount.get() shouldBe 1
                }
            }
        }
    }

    given("All workers are occupied") {
        occupyAllWorkers()

        `when`("A new Job is enqueued") {
            val result = createAndExecuteJob()

            then("The job is not executed") {
                result shouldBe false
            }
        }

        and("A currently running job is finished") {
            val fakeJob = fakeJobs.first()
            finishJob(fakeJob)

            then("It should be executed") {
                val lastFakeJob = fakeJobs.last()
                lastFakeJob.waitForExecution() shouldBe true
            }
        }
    }

    given("A FakeRecurringJob") {
        val recurringJob = FakeRecurringJob()

        `when`("The RecurringJob is executed") {
            classUnderTest.enqueue(recurringJob)
            recurringJob.waitForExecution()

            and("When it is canceled before being finished") {
                recurringJob.cancel()
                finishJob(recurringJob)

                then("It should not be executed again") {
                    recurringJob.waitForExecution() shouldBe false
                    recurringJob.atomicExecutionCount.get() shouldBe 1
                }
            }

            and("When it is finished") {
                finishJob(recurringJob)

                then("It should be executed again") {
                    recurringJob.waitForExecution() shouldBe true
                    recurringJob.atomicExecutionCount.get() shouldBe 2

                    recurringJob.cancel()
                    recurringJob.finish()
                }
            }
        }
    }

    given("An instable Job") {
        createAndExecuteJob()

        `when`("It throws an Exception") {
            fakeJobs.last().throwException()

            then("The Worker should accept new jobs") {
                occupyAllWorkers() shouldBe true
            }
        }
    }
})

open class FakeJob : ExecutorJob {
    val atomicExecutionCount = AtomicInteger(0)
    val atomicExecutionState = AtomicReference(State.Initialized)
    private val executionMutex = Any()
    private var throwException = false

    private var countDownLatch: CountDownLatch? = null

    override val recurringOptions: RecurringOptions? = null

    override fun run() {
        countDownLatch = CountDownLatch(1)
        synchronized(executionMutex) {
            atomicExecutionState.set(State.Executing)
            atomicExecutionCount.incrementAndGet()
        }

        countDownLatch?.await()

        if (throwException) {
            error("test exception")
        }

        synchronized(executionMutex) {
            atomicExecutionState.set(State.Finished)
        }
    }

    fun finish() {
        countDownLatch?.countDown()
    }

    fun throwException() {
        throwException = true
        finish()
    }

    fun waitForExecution(timeout: Long = 1, timeUnit: TimeUnit = TimeUnit.SECONDS): Boolean {
        val startTime = System.currentTimeMillis()
        while (true) {
            if (atomicExecutionState.get() == State.Executing) {
                return true
            }

            val currentTime = System.currentTimeMillis()
            val passedTime = currentTime - startTime
            if (passedTime >= timeUnit.toMillis(timeout)) {
                return false
            }
        }
    }

    fun waitForFinished(timeout: Long = 1, timeUnit: TimeUnit = TimeUnit.SECONDS): Boolean {
        val startTime = System.currentTimeMillis()
        while (true) {
            if (atomicExecutionState.get() == State.Finished) {
                return true
            }

            val currentTime = System.currentTimeMillis()
            val passedTime = currentTime - startTime
            if (passedTime >= timeUnit.toMillis(timeout)) {
                return false
            }
        }
    }
}

enum class State {
    Initialized,
    Executing,
    Finished,
}

class FakeRecurringJob : FakeJob() {
    override val recurringOptions: RecurringOptions = testRecurringOptions

    private val isCancelled: AtomicBoolean = AtomicBoolean(false)

    override fun run() {
        if (!isCancelled.get()) {
            super.run()
        }
    }

    override val shallRecur: Boolean
        get() = !isCancelled.get()

    /**
     * Prevents further execution of the function once called.
     */
    fun cancel() {
        isCancelled.set(true)
    }
}
