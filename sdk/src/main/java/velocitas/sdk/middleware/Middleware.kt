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

import velocitas.sdk.NoArgumentSingletonHolder
import velocitas.sdk.middleware.Middleware.Companion.TYPE_DEFINING_ENV_VAR_NAME
import velocitas.sdk.middleware.Middleware.Companion.getEnvVar

abstract class Middleware protected constructor(
    /**
     * The type identifier of the concrete middleware implementation.
     */
    val typeId: String,
) {
    private val metadata: Metadata = Metadata()

    /**
     * Triggers the start of the middleware.
     */
    open fun start() {
        // empty default implementation
    }

    /**
     * Waits (and blocks current thread) until the middleware is started and ready to use.
     */
    open fun waitUntilReady() {
        // empty default implementation
    }

    /**
     * Stops the middleware.
     */
    open fun stop() {
        // empty default implementation
    }

    /**
     * Get the location description (e.g. uri) for the specified [serviceName] and returns the location of this service.
     *
     * @throws RuntimeException will be thrown if the service location could not be determined.
     */
    open fun getServiceLocation(serviceName: String): String {
        return ""
    }

    /**
     * Get the middleware specific metadata needed to communicate with the specified service.
     *
     * @param serviceName Name of the service to communicate with
     * @return Metadata
     */
    open fun getMetadata(serviceName: String): Map<String, String> {
        return metadata.toMap()
    }

    companion object : NoArgumentSingletonHolder<Middleware>({
        val middlewareType = getEnvVar(TYPE_DEFINING_ENV_VAR_NAME).lowercase()
        if (middlewareType.isEmpty()) {
            NativeMiddleware()
        } else if (middlewareType == NativeMiddleware.TYPE_ID) {
            NativeMiddleware()
        } else {
            error("Unknown middleware type '$middlewareType'")
        }
    }) {
        /**
         * Defines the name of the environment variable used to determine the middleware type to
         * be used.
         */
        const val TYPE_DEFINING_ENV_VAR_NAME = "SDV_MIDDLEWARE_TYPE"

        /**
         * Retrieves the environment variable for the given [varName] and returns it's value. If no environment variable
         * exist the [defaultValue] will be returned.
         */
        fun getEnvVar(varName: String, defaultValue: String = ""): String {
            val envVar = System.getenv(varName)
            if (envVar != null) {
                return envVar
            }
            return defaultValue
        }
    }
}
