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

import java.util.Date

/**
 * ConsoleLoggingStrategy logs the message to System.out and System.err depending on the log level.
 */
object ConsoleLoggingStrategy : LoggingStrategy {
    override fun verbose(tag: String, message: String) {
        val formattedMsg = format("VERBOSE", tag, message)
        println(formattedMsg)
    }

    override fun debug(tag: String, message: String) {
        val formattedMsg = format("DEBUG", tag, message)
        println(formattedMsg)
    }

    override fun info(tag: String, message: String) {
        val formattedMsg = format("INFO", tag, message)
        println(formattedMsg)
    }

    override fun warn(tag: String, message: String) {
        val formattedMsg = format("WARN", tag, message)
        println(formattedMsg)
    }

    override fun error(tag: String, message: String) {
        val formattedMsg = format("ERROR", tag, message)
        System.err.println(formattedMsg)
    }

    private fun format(
        level: String,
        tag: String,
        msg: String,
    ): String {
        return "${Date()} - $level - $tag: $msg"
    }
}
