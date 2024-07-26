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

import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor

private const val DEFAULT_NUMBER_WORKER_THREADS = 2

/**
 * Manages a pool of threads which are capable of executing jobs asynchronously.
 */
class ThreadPool(var numWorkerThreads: Int) {
    constructor() : this(DEFAULT_NUMBER_WORKER_THREADS)

    private val scheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(numWorkerThreads)
        .apply {
            removeOnCancelPolicy = true
        }

    private val recurringJobs = mutableMapOf<ExecutorJob, ScheduledFuture<*>>()

    private var isShutdown: Boolean = false

    /**
     * Enqueue a new [job] to be processed by one of the worker threads. No more than [numWorkerThreads] jobs are
     * processed at the same time.
     */
    fun enqueue(job: ExecutorJob) {
        check(!isShutdown) {
            "Cannot enqueue new jobs. ThreadPool has already been shutdown"
        }

        if (job.shallRecur) {
            val recurringOptions = job.recurringOptions!!
            val scheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(
                job,
                recurringOptions.initialDelay,
                recurringOptions.period,
                recurringOptions.unit,
            )

            recurringJobs[job] = scheduledFuture
        } else {
            scheduledThreadPoolExecutor.submit(job)
        }
    }

    fun cancelRecurringJob(executorJob: ExecutorJob): Boolean {
        check(executorJob.shallRecur) {
            "Not a recurring Job: $executorJob"
        }

        val scheduledFuture = recurringJobs.remove(executorJob)
        return scheduledFuture?.cancel(true) == true
    }

    /**
     * The ThreadPool will finish it's currently running jobs and no longer process new jobs.
     */
    fun shutdown() {
        isShutdown = true
        scheduledThreadPoolExecutor.shutdown()
    }
}
