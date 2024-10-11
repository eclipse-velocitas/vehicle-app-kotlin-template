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

package org.eclipse.velocitas.sdk.logging

import org.eclipse.velocitas.sdk.logging.Logger.loggingStrategy

/**
 * Component used for Logging. The underlying strategy how to log can be changed by calling the [loggingStrategy]
 * method.
 */
object Logger {
    /**
     * Changes the underlying strategy how to logging strategy.
     */
    var loggingStrategy: LoggingStrategy = ConsoleLoggingStrategy

    /**
     * Logs a [message] with the specified [tag] on verbose level and uses the variadic list of [messageArgs]
     * to replace them within the provided message.
     */
    fun verbose(
        tag: String,
        message: String,
        vararg messageArgs: Any?,
    ) {
        val formattedMessage = message.format(messageArgs)
        loggingStrategy.verbose(tag, formattedMessage)
    }

    /**
     * Logs a [message] with the specified [tag] on debug level and uses the variadic list of [messageArgs]
     * to replace them within the provided message.
     */
    fun debug(
        tag: String,
        message: String,
        vararg messageArgs: Any?,
    ) {
        val formattedMsg = message.format(messageArgs)
        loggingStrategy.debug(tag, formattedMsg)
    }

    /**
     * Logs a [message] with the specified [tag] on info level and uses the variadic list of [messageArgs]
     * to replace them within the provided message.
     */

    fun info(
        tag: String,
        message: String,
        vararg messageArgs: Any?,
    ) {
        val formattedMsg = message.format(messageArgs)
        loggingStrategy.info(tag, formattedMsg)
    }

    /**
     * Logs a [message] with the specified [tag] on warn level and uses the variadic list of [messageArgs]
     * to replace them within the provided message.  A [throwable] can be provided as a cause for the warn
     * behavior.
     */
    fun warn(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        vararg messageArgs: Any?,
    ) {
        var formattedMsg = message.format(messageArgs)
        if (throwable != null) {
            formattedMsg += ": ${System.lineSeparator()} ${throwable.message}"
        }
        loggingStrategy.warn(tag, formattedMsg)
    }

    /**
     * Logs a [message] with the specified [tag] on warn level and uses the variadic list of [messageArgs]
     * to replace them within the provided message.
     */
    fun warn(
        tag: String,
        message: String,
        vararg messageArgs: Any?,
    ) {
        warn(tag, message, null, messageArgs)
    }

    /**
     * Logs a [message] with the specified [tag] on error level and uses the variadic list of [messageArgs]
     * to replace them within the provided message.
     */
    fun error(
        tag: String,
        message: String,
        vararg messageArgs: Any?,
    ) {
        error(tag, message, null, messageArgs)
    }

    /**
     * Logs a [message] with the specified [tag] on verbose level and uses the variadic list of [messageArgs]
     * to replace them within the provided message. A [throwable] can be provided as a cause for the erroneous
     * behavior.
     */
    fun error(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        vararg messageArgs: Any?,
    ) {
        var formattedMsg = message.format(messageArgs)
        if (throwable != null) {
            formattedMsg += ": ${System.lineSeparator()} ${throwable.message}"
        }
        loggingStrategy.error(tag, formattedMsg)
    }
}
