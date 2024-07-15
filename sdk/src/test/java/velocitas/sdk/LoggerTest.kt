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
