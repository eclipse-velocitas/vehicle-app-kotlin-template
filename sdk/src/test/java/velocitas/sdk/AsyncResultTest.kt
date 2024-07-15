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

import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.milliseconds

class AsyncResultTest : BehaviorSpec({
    given("An AsyncResult with an already buffered result") {
        val asyncResult = AsyncResult<Int>()

        val expectedResult = 10
        asyncResult.insertResult(expectedResult)

        `when`("Await is called") {
            val result = asyncResult.await()
            then("It should immediately return the buffered result") {
                result shouldBe expectedResult
            }
        }
    }

    given("An AsyncResult w/o an already buffered result") {
        val asyncResult = AsyncResult<Int>()
        val expectedResult = 100

        thread {
            Thread.sleep(50)
            asyncResult.insertResult(expectedResult)
        }

        `when`("Await is called") {
            val result = asyncResult.await()

            then("It will block the thread until the result is available") {
                result shouldBe expectedResult
            }
        }
    }

    given("An AsyncResult which is already waiting for a result") {
        val asyncResult = AsyncResult<Int>()

        thread {
            asyncResult.await()
        }

        while (!asyncResult.isInAwaitingState()) {
            Thread.sleep(1)
        }

        `when`("Calling the onResult callback") {
            val result = runCatching {
                asyncResult.onResult {
                    // do nothing
                }
            }
            then("It should throw an Exception") {
                result.isFailure shouldBe true
                result.exceptionOrNull() shouldNotBeNull {}
            }
        }
    }

    given("An AsyncResult with an onResult handler") {
        var receivedResult = -1

        val asyncResult = AsyncResult<Int>()
        asyncResult.onResult { receivedResult = it }

        `when`("The result is received") {
            val expectedResult = 1000
            asyncResult.insertResult(expectedResult)

            then("It will be correctly executed") {
                eventually(50.milliseconds) {
                    receivedResult shouldBe expectedResult
                }
            }
        }
    }

    given("An AsyncResult with a dummy onResult handler") {
        val asyncResult = AsyncResult<Int>()
        asyncResult.onResult {
            // do nothing
        }

        `when`("Await is called") {
            val result = runCatching {
                asyncResult.await()
            }

            then("It should throw an Exception") {
                result.isFailure shouldBe true
                result.exceptionOrNull() shouldNotBeNull { }
            }
        }
    }
})
