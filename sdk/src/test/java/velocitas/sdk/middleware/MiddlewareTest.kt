package velocitas.sdk.middleware

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class MiddlewareTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerTest

    context("Middleware.getInstance() returns NativeMiddleware per default") {
        `when`("Retrieving the typeId of the Middleware") {
            val instance = Middleware.getInstance()
            val typeId = instance.typeId

            then("It should return the native Middleware") {
                typeId shouldBe NativeMiddleware.TYPE_ID
            }
        }
    }

    context("Middleware.getInstance() returns NativeMiddleware") {
        given("EnvVar '${Middleware.TYPE_DEFINING_ENV_VAR_NAME}' is set to '${NativeMiddleware.TYPE_ID}'") {
            withEnvironment(key = Middleware.TYPE_DEFINING_ENV_VAR_NAME, NativeMiddleware.TYPE_ID) {
                `when`("Retrieving the Middleware") {
                    val instance = Middleware.getInstance()
                    val typeId = instance.typeId

                    then("It should be of type NativeMiddleware") {
                        typeId shouldBe NativeMiddleware.TYPE_ID
                    }
                }
            }
        }
    }

    context("Middleware.getInstance() throws an Exception when an unknown Middleware is set") {
        given("EnvVar '${Middleware.TYPE_DEFINING_ENV_VAR_NAME}' is set to 'unknown'") {
            withEnvironment(key = Middleware.TYPE_DEFINING_ENV_VAR_NAME, value = "unknown") {
                `when`("Retrieving the Middleware") {
                    val result = runCatching {
                        Middleware.getInstance()
                    }

                    then("An Exception should be thrown") {
                        result.isFailure shouldBe true
                        result.exceptionOrNull() shouldNotBeNull { }
                    }
                }
            }
        }
    }
})
