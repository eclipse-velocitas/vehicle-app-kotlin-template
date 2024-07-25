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

package velocitas.sdk.logging

/**
 * Component used for Logging. The underlying strategy how to log can be changed by calling the [loggingStrategy]
 * method.
 */
object Logger {
    /**
     * Changes the underlying strategy how to logging strategy.
     */
    var loggingStrategy: LoggingStrategy = ConsoleLoggingStrategy()

    /**
     * Logs a [message] with debug level and uses the variadic list of [arguments] to replace them within the provided
     * message.
     */
    fun debug(
        message: String,
        vararg arguments: Any?,
    ) {
        val formattedMsg = message.format(arguments)
        loggingStrategy.debug(formattedMsg)
    }

    /**
     * Logs a [message] with info level and uses the variadic list of [arguments] to replace them within the provided
     * message.
     */
    fun info(
        message: String,
        vararg arguments: Any?,
    ) {
        val formattedMsg = message.format(arguments)
        loggingStrategy.info(formattedMsg)
    }

    /**
     * Logs a [message] with warn level and uses the variadic list of [arguments] to replace them within the provided
     * message.
     */
    fun warn(
        message: String,
        throwable: Throwable? = null,
        vararg arguments: Any?,
    ) {
        var formattedMsg = message.format(arguments)
        if (throwable != null) {
            formattedMsg += ": ${System.lineSeparator()} ${throwable.message}"
        }
        loggingStrategy.warn(formattedMsg)
    }

    /**
     * Logs a [message] with info level and uses the variadic list of [arguments] to replace them within the provided
     * message.
     */
    fun warn(
        message: String,
        vararg arguments: Any?,
    ) {
        warn(message, null, arguments)
    }

    /**
     * Logs a [message] with error level and uses the variadic list of [arguments] to replace them within the provided
     * message.
     */
    fun error(
        message: String,
        vararg arguments: Any?,
    ) {
        error(message, null, arguments)
    }

    /**
     * Logs a [message] with error level and uses the variadic list of [arguments] to replace them within the provided
     * message.
     */
    fun error(
        message: String,
        throwable: Throwable? = null,
        vararg arguments: Any?,
    ) {
        var formattedMsg = message.format(arguments)
        if (throwable != null) {
            formattedMsg += ": ${System.lineSeparator()} ${throwable.message}"
        }
        loggingStrategy.error(formattedMsg)
    }
}
