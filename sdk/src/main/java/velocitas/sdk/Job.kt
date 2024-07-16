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

import java.util.concurrent.atomic.AtomicBoolean

interface ExecutorJob {

    /**
     * Execute the job.
     */
    fun execute()

    /**
     * Indicates if this job shall recur after its execution.
     *
     * @return true - recur this job
     * @return false - don't recur
     */
    fun shallRecur(): Boolean {
        return false
    }
}

/**
 * A non-recurring job. Will be executed once and afterwards removed from the JobQueue. Should be used together with
 * the [ThreadPool].
 *
 */
open class Job(
    /**
     * Lambda Function to be executed.
     */
    private val function: (() -> Unit),
) : ExecutorJob {

    @Synchronized
    override fun execute() {
        function()
    }

    /**
     * Blocks other threads until [execute] finished successful.
     */
    @Synchronized
    fun waitForTermination() {
        // blocking is achieved here by using @Synchronized annotation
    }
}

/**
 * A recurring job. Will be executed continuously until cancel is called or the [ThreadPool] is stopped. Once the
 * Job finished successfully it will be added again to the working queue. This means the time between two consecutive
 * executions of the recurring job might vary.
 */
open class RecurringJob(function: (() -> Unit)) : Job(function) {
    private val isCancelled: AtomicBoolean = AtomicBoolean(false)

    override fun execute() {
        if (!isCancelled.get()) {
            super.execute()
        }
    }

    override fun shallRecur(): Boolean {
        return !isCancelled.get()
    }

    /**
     * Prevents further execution of the function once called.
     */
    fun cancel() {
        isCancelled.set(true)
    }
}
