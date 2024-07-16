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

import velocitas.sdk.logging.VelocitasLogger
import java.util.Vector
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

private const val MAX_NUMBER_PENDING_JOBS = 1_000
private const val DEFAULT_NUMBER_WORKER_THREADS = 2

/**
 * Manages a pool of threads which are capable of executing jobs asynchronously.
 */
class ThreadPool(var numWorkerThreads: Int) {
    constructor() : this(DEFAULT_NUMBER_WORKER_THREADS)

    private val workerThreads = Vector<Thread>()
    private val jobs = ArrayBlockingQueue<ExecutorJob>(MAX_NUMBER_PENDING_JOBS)
    private val isRunning: AtomicBoolean = AtomicBoolean(true)

    init {
        repeat(numWorkerThreads) {
            val thread = thread {
                threadLoop()
            }

            workerThreads.add(thread)
        }
    }

    fun enqueue(job: ExecutorJob) {
        jobs.add(job)
    }

    fun shutdown() {
        isRunning.set(false)
    }

    @Suppress("TooGenericExceptionCaught") // third party code could throw any exception
    private fun threadLoop() {
        while (isRunning.get()) {
            val polledJob = jobs.poll() ?: continue

            try {
                polledJob.execute()
                if (polledJob.shallRecur()) {
                    enqueue(polledJob)
                }
            } catch (e: Exception) {
                VelocitasLogger.error("Uncaught Exception in job execution: ${e.message}")
            }
        }
    }

    companion object : NoArgumentSingletonHolder<ThreadPool>(::ThreadPool)
}
