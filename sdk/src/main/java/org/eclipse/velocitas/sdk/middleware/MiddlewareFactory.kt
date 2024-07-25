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

package org.eclipse.velocitas.sdk.middleware

class MiddlewareFactory {
    @Suppress("MoveVariableDeclarationIntoWhen")
    fun create(): Middleware {
        val envVar = System.getenv(TYPE_DEFINING_ENV_VAR_NAME) ?: ""
        val middlewareType = envVar.lowercase().trim()

        return when (middlewareType) {
            EMPTY_STRING,
            NativeMiddleware.TYPE_ID,
            -> {
                NativeMiddleware()
            }

            else -> {
                error("Unknown middleware type '$middlewareType'")
            }
        }
    }

    companion object {
        const val TYPE_DEFINING_ENV_VAR_NAME = "SDV_MIDDLEWARE_TYPE"

        private const val EMPTY_STRING = ""
    }
}
