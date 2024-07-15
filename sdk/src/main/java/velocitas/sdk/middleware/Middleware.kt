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

import velocitas.getEnvVar

abstract class Middleware protected constructor(
    /**
     * The type identifier of the concrete middleware implementation
     */
    val typeId: String,
) {
    private val metadata: Metadata = Metadata()

    /**
     * Triggers the start of the middleware
     */
    open fun start() { }

    /**
     * Waits (blocks current thread) until the middleware is started and ready to use
     */
    open fun waitUntilReady() { }

    /**
     * Stops the middleware
     */
    open fun stop() { }

    /**
     * Get the location description (e.g. uri) of the specified service name
     *
     * @param serviceName Name of the service to get the location description for
     * @return representing the location description
     * @throws RuntimeException if the service location cannot be determined
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

    companion object {
        /**
         * Defines the name of the environment variable used to determine the middleware type to
         * be used.
         */
        const val TYPE_DEFINING_ENV_VAR_NAME = "SDV_MIDDLEWARE_TYPE"

        /**
         * Returns a reference to a singleton instance of a concrete middleware class
         *
         * @return Middleware&
         */
        fun getInstance(): Middleware {
            if (instance == null) {
                instance = instantiate()
            }

            return instance as Middleware
        }

        @Volatile
        private var instance: Middleware? = null

        private fun instantiate(): Middleware {
            val middlewareType = getEnvVar(TYPE_DEFINING_ENV_VAR_NAME).lowercase()
            return if (middlewareType.isEmpty()) {
                NativeMiddleware()
            } else if (middlewareType == NativeMiddleware.TYPE_ID) {
                NativeMiddleware()
            } else {
                throw RuntimeException("Unknown middleware type '$middlewareType'")
            }
        }
    }
}
