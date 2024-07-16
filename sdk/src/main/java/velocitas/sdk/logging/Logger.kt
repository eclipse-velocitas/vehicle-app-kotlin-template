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

import java.util.Date

/**
 * A generic logger interface. Can be used to switch the underlying logging strategy of the VelocitasLogger.
 */
interface Logger {
    /**
     * Log a [message] with info level.
     */
    fun info(message: String)

    /**
     * Log a [message] with warn level.
     */
    fun warn(message: String)

    /**
     * Log a [message] with error level.
     */
    fun error(message: String)

    /**
     * Log a [message] with debug level.
     */
    fun debug(message: String)
}

/**
 * Component used for Logging. The underlying strategy how to log can be switched using the [setLoggerImplementation]
 * method.
 */
object VelocitasLogger {
    private var impl: Logger = ConsoleLogger()

    /**
     * Logs a [message] with debug level and uses the variadic list of [arguments] to replace them within the provided
     * message.
     */
    fun debug(message: String, vararg arguments: Any?) {
        val formattedMsg = message.format(arguments)
        impl.debug(formattedMsg)
    }

    /**
     * Logs a [message] with info level and uses the variadic list of [arguments] to replace them within the provided
     * message.
     */
    fun info(message: String, vararg arguments: Any?) {
        val formattedMsg = message.format(arguments)
        impl.info(formattedMsg)
    }

    /**
     * Logs a [message] with info level and uses the variadic list of [arguments] to replace them within the provided
     * message.
     */
    fun warn(message: String, vararg arguments: Any?) {
        val formattedMsg = message.format(arguments)
        impl.warn(formattedMsg)
    }

    /**
     * Logs a [message] with info level and uses the variadic list of [arguments] to replace them within the provided
     * message.
     */
    fun error(message: String, vararg arguments: Any?) {
        val formattedMsg = message.format(arguments)
        impl.error(formattedMsg)
    }

    /**
     * Switches the underlying strategy how to log messages.
     */
    fun setLoggerImplementation(impl: Logger) {
        VelocitasLogger.impl = impl
    }
}

class ConsoleLogger : Logger {
    override fun debug(message: String) {
        val formattedMsg = format("DEBUG", message)
        println(formattedMsg)
        System.out.flush()
    }

    override fun info(message: String) {
        val formattedMsg = format("INFO", message)
        println(formattedMsg)
        System.out.flush()
    }

    override fun warn(message: String) {
        val formattedMsg = format("WARN", message)
        println(formattedMsg)
        System.out.flush()
    }

    override fun error(message: String) {
        val formattedMsg = format("ERROR", message)
        System.err.println(formattedMsg)
        System.err.flush()
    }

    private fun format(level: String, msg: String): String {
        return "${Date()}, $level: $msg"
    }
}
