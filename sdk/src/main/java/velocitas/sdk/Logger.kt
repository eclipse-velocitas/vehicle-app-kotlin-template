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

import java.util.Date

interface ILogger {
    /**
     * Log a message with info level.
     *
     * @param msg The message to log.
     */
    fun info(msg: String)

    /**
     * Log a message with warn level.
     *
     * @param msg The message to log.
     */
    fun warn(msg: String)

    /**
     * Log a message with error level.
     *
     * @param msg The message to log.
     */
    fun error(msg: String)

    /**
     * Log a message with debug level.
     *
     * @param msg The message to log.
     */
    fun debug(msg: String)
}

object Logger {
    private var impl: ILogger = ConsoleLogger()

    /**
     * Log a message with debug level.
     *
     * @param msg   The format message.
     * @param args  The format arguments.
     */
    fun debug(msg: String, vararg args: Any?) {
        val formattedMsg = msg.format(args)
        impl.debug(formattedMsg)
    }

    /**
     * Log a message with info level.
     *
     * @param msg   The format message.
     * @param args  The format arguments.
     */
    fun info(msg: String, vararg args: Any?) {
        val formattedMsg = msg.format(args)
        impl.info(formattedMsg)
    }

    /**
     * Log a message with warn level.
     *
     * @param msg   The format message.
     * @param args  The format arguments.
     */
    fun warn(msg: String, vararg args: Any?) {
        val formattedMsg = msg.format(args)
        impl.warn(formattedMsg)
    }

    /**
     * Log a message with error level.
     *
     * @param msg   The format message.
     * @param args  The format arguments.
     */
    fun error(msg: String, vararg args: Any?) {
        val formattedMsg = msg.format(args)
        impl.error(formattedMsg)
    }

    fun setLoggerImplementation(impl: ILogger) {
        this.impl = impl
    }
}

class ConsoleLogger: ILogger {
    override fun debug(msg: String) {
        val formattedMsg = format("DEBUG", msg)
        println(formattedMsg)
        System.out.flush()
    }

    override fun info(msg: String) {
        val formattedMsg = format("INFO", msg)
        println(formattedMsg)
        System.out.flush()
    }

    override fun warn(msg: String) {
        val formattedMsg = format("WARN", msg)
        println(formattedMsg)
        System.out.flush()
    }

    override fun error(msg: String) {
        val formattedMsg = format("ERROR", msg)
        System.err.println(formattedMsg)
        System.err.flush()
    }

    private fun format(level: String, msg: String): String {
        return "${Date()}, $level: $msg"
    }
}
