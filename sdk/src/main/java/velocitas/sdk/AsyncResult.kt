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

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Empty result structure which can be used in case an AsyncResult
 *        does not return any valuable information other than its success.
 */
class VoidResult

class AsyncResult<TResultType> {
    private var result: TResultType? = null
    private var resultCallback: ((TResultType) -> Unit)? = null
    private var errorCallback: ((Status) -> Unit)? = null
    private val isAwaiting: AtomicBoolean = AtomicBoolean(false)
    private var status: Status = Status()

    private val countDownLatch = CountDownLatch(1)

    /**
     * Inserts the result and notifies any waiters.
     *
     * @param result  Result to insert.
     */
    fun insertResult(result: TResultType) {
        try {
            if (resultCallback != null) {
                resultCallback?.invoke(result)
            } else {
                this.result = result
            }
        } finally {
            countDownLatch.countDown()
        }
    }

    /**
     * Inserts a new error and notifies any waiters.
     *
     * @param error Status containing error information.
     */
    fun insertError(error: Status) {
        try {
            if (errorCallback != null) {
                errorCallback?.invoke(error)
            } else {
                this.status = error
            }
        } finally {
            countDownLatch.countDown()
        }
    }

    /**
     * Blocks the calling thread until the result is available.
     *
     * @throw AsyncException     if there is any issues during async invocation.
     * @throw RuntimeException   if the API usage is wrong.
     *
     * @return TResultType    Result of the async operation once it completes.
     */
    fun await(): TResultType {
        check(resultCallback == null) {
            "Invalid usage: Either call await() or register an onResult callback!"
        }

        isAwaiting.set(true)
        countDownLatch.await()

        if (status.isOk) {
            return result!!
        }

        throw AsyncException(status.errorMessage ?: "")
    }

    /**
     * Calls the specified callback when the result is available.
     *        The callback invocation is done by a worker thread.
     *
     * @param onResultCallback The callback to invoke.
     * @return AsyncResult     This for method chaining.
     */
    fun onResult(onResultCallback: ((TResultType) -> Unit)): AsyncResult<TResultType> {
        check(!isAwaiting.get()) {
            "Invalid usage: Either call await() or register an onResult callback!"
        }
        resultCallback = onResultCallback

        return this
    }

    /**
     * Calls the specified callback when an error occurs during async execution.
     *        The callback invocation is done by a worker thread.
     *
     * @param onErrorCallback The callback to invoke.
     * @return AsyncResult    This for method chaining.
     */
    fun onError(onErrorCallback: ((Status) -> Unit)): AsyncResult<TResultType> {
        errorCallback = onErrorCallback

        return this
    }

    /**
     * @brief Return if the result is currently being awaited.
     *
     * @return true if the AsyncResult is being waited on, or false otherwise
     */
    fun isInAwaitingState(): Boolean {
        return isAwaiting.get()
    }
}
