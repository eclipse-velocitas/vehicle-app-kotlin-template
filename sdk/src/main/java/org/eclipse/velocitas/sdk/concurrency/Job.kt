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

interface ExecutorJob : Runnable {
    /**
     * Indicates if this job shall recur after its execution. If the job shall recur the corresponding
     * [recurringOptions] need to be set.
     */
    val shallRecur: Boolean
        get() = recurringOptions != null

    /**
     * The recurringOptions to use when executing the task multiple times. It allows a more fine granular adjustment
     * of initial delays or execution periods between two consecutive executions.
     */
    val recurringOptions: RecurringOptions?
}

/**
 * A non-recurring job. Will be executed once and afterwards removed from the JobQueue. Should be used together with
 * the [ThreadPool].
 */
open class Job(
    /**
     * Lambda Function to be executed.
     */
    private val function: (() -> Unit),
) : ExecutorJob {
    override val recurringOptions: RecurringOptions? = null

    @Synchronized
    override fun run() {
        function()
    }

    /**
     * Blocks other threads until [run] finished successful.
     */
    @Synchronized
    fun waitForTermination() {
        // blocking is achieved here by using @Synchronized annotation
    }
}

/**
 * A recurring job. Will be executed continuously until [ThreadPool.cancelRecurringJob] is called. Once the
 * Job finished successfully it will be added again to the working queue. This means the time between two consecutive
 * executions of the recurring job might vary. Should be used together with the [ThreadPool].
 */
open class RecurringJob(override val recurringOptions: RecurringOptions, function: (() -> Unit)) : Job(function)
