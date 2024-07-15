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

import io.kotest.core.config.LogLevel
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class LoggerTest : BehaviorSpec({
    Logger.setLoggerImplementation(StringLogger)

    given("An errorMessage w/o arguments") {
        val errorMessage = "Error: Some Error Occurred - Errorcode: "
        var counter = 0

        `when`("Logging on Debug Level") {
            counter++
            Logger.debug("$errorMessage ($counter)")

            then("It should log the correct message on the correct level") {
                StringLogger.lastLevel = LogLevel.Debug
                StringLogger.lastMessage = "$errorMessage ($counter)"
            }
        }

        `when`("Logging on Info Level") {
            counter++
            Logger.info("$errorMessage ($counter)")

            then("It should log the correct message on the correct level") {
                StringLogger.lastLevel = LogLevel.Info
                StringLogger.lastMessage = "$errorMessage ($counter)"
            }
        }

        `when`("Logging on Warn Level") {
            counter++
            Logger.warn("$errorMessage ($counter)")

            then("It should log the correct message on the correct level") {
                StringLogger.lastLevel = LogLevel.Warn
                StringLogger.lastMessage = "$errorMessage ($counter)"
            }
        }

        `when`("Logging on Error Level") {
            counter++

            Logger.error("$errorMessage ($counter)")

            then("It should log the correct message on the correct level") {
                StringLogger.lastLevel = LogLevel.Error
                StringLogger.lastMessage = "$errorMessage ($counter)"
            }
        }
    }

    given("An errorMessage w/ arguments") {
        val errorMessage = "Error: Some Error Occurred"
        var counter = 0

        `when`("Logging on Debug Level") {
            counter++
            Logger.debug("$errorMessage (%s)", counter)

            then("It should log the correct message on the correct level") {
                StringLogger.lastLevel = LogLevel.Debug
                StringLogger.lastMessage = "$errorMessage ($counter)"
            }
        }

        `when`("Logging on Info Level") {
            counter++
            Logger.info("$errorMessage (%s)", counter)

            then("It should log the correct message on the correct level") {
                StringLogger.lastLevel = LogLevel.Info
                StringLogger.lastMessage = "$errorMessage ($counter)"
            }
        }

        `when`("Logging on Warn Level") {
            counter++
            Logger.warn("$errorMessage (%s)", counter)

            then("It should log the correct message on the correct level") {
                StringLogger.lastLevel = LogLevel.Warn
                StringLogger.lastMessage = "$errorMessage ($counter)"
            }
        }

        `when`("Logging on Error Level") {
            counter++
            Logger.error("$errorMessage (%s)", counter)

            then("It should log the correct message on the correct level") {
                StringLogger.lastLevel = LogLevel.Error
                StringLogger.lastMessage = "$errorMessage ($counter)"
            }
        }
    }
})

object StringLogger : ILogger {
    var lastLevel: LogLevel? = null
    var lastMessage: String? = null

    override fun info(msg: String) {
        lastLevel = LogLevel.Info
        lastMessage = msg
    }

    override fun warn(msg: String) {
        lastLevel = LogLevel.Warn
        lastMessage = msg
    }

    override fun error(msg: String) {
        lastLevel = LogLevel.Error
        lastMessage = msg
    }

    override fun debug(msg: String) {
        lastLevel = LogLevel.Debug
        lastMessage = msg
    }
}
