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

import org.eclipse.velocitas.sdk.logging.Logger
import org.eclipse.velocitas.sdk.parser.SimpleUrlParser

/**
 * Native Middleware implementation.
 */
class NativeMiddleware : Middleware(TYPE_ID) {

    override fun findServiceLocation(serviceName: String): String {
        val envVarName = getServiceEnvVarName(serviceName)
        val envVar = System.getenv(envVarName) ?: ""

        val simpleUrlParser = SimpleUrlParser()
        val result: SimpleUrlParser.Result = simpleUrlParser.parse(envVar)
        val serviceAddress = result.netLocation

        if (serviceAddress.isNotEmpty()) {
            return serviceAddress
        }

        val defaultServiceAddress = getDefaultLocation(serviceName)
        if (defaultServiceAddress?.isNotEmpty() == true) {
            Logger.warn(
                "Env variable '$envVarName' defining location of " +
                    "service '$serviceName' not properly set. Taking default: '$serviceAddress'",
            )
            return defaultServiceAddress
        }

        val errorMessage = "Env variable '$envVarName' defining location of " +
            "service '$serviceName' not set. Please define!"
        Logger.error(errorMessage)
        throw IllegalStateException(errorMessage)
    }

    companion object {
        const val TYPE_ID = "native"

        val DEFAULT_LOCATIONS = mapOf(
            "mqtt" to "localhost:1883",
            "vehicledatabroker" to "localhost:55555",
        )

        fun getDefaultLocation(serviceName: String): String? {
            val filteredService =
                DEFAULT_LOCATIONS.entries.find { it.key == serviceName.lowercase() }
            if (filteredService != null) {
                val simpleUrlParser = SimpleUrlParser()
                val result = simpleUrlParser.parse(filteredService.value)
                return result.netLocation
            }
            return null
        }

        fun getServiceEnvVarName(serviceName: String): String {
            return "SDV_${serviceName.uppercase()}_ADDRESS"
        }
    }
}
