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

package velocitas.sdk.middleware

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class MiddlewareTest : BehaviorSpec({
    val middlewareFactory = MiddlewareFactory()

    context("Middleware.getInstance() returns NativeMiddleware per default") {
        `when`("Retrieving the typeId of the Middleware") {
            val middleware = middlewareFactory.create()
            val typeId = middleware.typeId

            then("It should return the native Middleware") {
                typeId shouldBe NativeMiddleware.TYPE_ID
            }
        }
    }

    context("Middleware.getInstance() returns NativeMiddleware") {
        given("EnvVar '${MiddlewareFactory.TYPE_DEFINING_ENV_VAR_NAME}' is set to '${NativeMiddleware.TYPE_ID}'") {
            withEnvironment(key = MiddlewareFactory.TYPE_DEFINING_ENV_VAR_NAME, NativeMiddleware.TYPE_ID) {
                `when`("Retrieving the Middleware") {
                    val middleware = middlewareFactory.create()
                    val typeId = middleware.typeId

                    then("It should be of type NativeMiddleware") {
                        typeId shouldBe NativeMiddleware.TYPE_ID
                    }
                }
            }
        }
    }

    context("Middleware.getInstance() throws an Exception when an unknown Middleware is set") {
        given("EnvVar '${MiddlewareFactory.TYPE_DEFINING_ENV_VAR_NAME}' is set to 'unknown'") {
            withEnvironment(key = MiddlewareFactory.TYPE_DEFINING_ENV_VAR_NAME, value = "unknown") {
                `when`("Retrieving the Middleware") {
                    val result = runCatching {
                        middlewareFactory.create()
                    }

                    then("An Exception should be thrown") {
                        result.isFailure shouldBe true
                        result.exceptionOrNull() shouldNotBeNull { }
                    }
                }
            }
        }
    }
})
