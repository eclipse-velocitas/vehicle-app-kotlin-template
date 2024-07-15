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

interface IJob {

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
 * A nonrecurring job.
 */
open class Job(private val function: (() -> Unit)) : IJob {
    @Synchronized
    override fun execute() {
        function()
    }

    @Synchronized
    fun waitForTermination() {
    }
}

/**
 * A recurring job which can be cancelled manually.
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
     * Prevents execution of the function once called.
     */
    fun cancel() {
        isCancelled.set(true)
    }
}
