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

package velocitas.sdk.grpc

import velocitas.sdk.RecurringJob
import velocitas.sdk.ThreadPool

/**
 * GrpcClient used to communicate with a GrpcService. It tracks the number of active GrpcCalls.
 */
open class GrpcClient {
    private val recurringJob: RecurringJob
    private val activeCalls = mutableListOf<GrpcCall>()

    val numActiveCalls: Int
        get() {
            return activeCalls.size
        }

    init {
        recurringJob = RecurringJob {
            pruneCompletedRequests()
        }
        ThreadPool.getInstance().enqueue(recurringJob)
    }

    /**
     * Adds an active GrpcCall.
     */
    @Synchronized
    fun addActiveCall(call: GrpcCall) {
        activeCalls.add(call)
    }

    @Synchronized
    private fun pruneCompletedRequests() {
        val iterator = activeCalls.iterator()
        iterator.forEach { grpcCall ->
            if (grpcCall.isComplete) {
                iterator.remove()
            }
        }
    }
}
