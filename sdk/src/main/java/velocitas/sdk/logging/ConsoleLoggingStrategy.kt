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
 * ConsoleLoggingStrategy logs the message to System.out and System.err depending on the log level.
 */
class ConsoleLoggingStrategy : LoggingStrategy {
    override fun debug(message: String) {
        val formattedMsg = format("DEBUG", message)
        println(formattedMsg)
    }

    override fun info(message: String) {
        val formattedMsg = format("INFO", message)
        println(formattedMsg)
    }

    override fun warn(message: String) {
        val formattedMsg = format("WARN", message)
        println(formattedMsg)
    }

    override fun error(message: String) {
        val formattedMsg = format("ERROR", message)
        System.err.println(formattedMsg)
    }

    private fun format(
        level: String,
        msg: String,
    ): String {
        return "${Date()}, $level: $msg"
    }
}
