package velocitas.sdk.middleware

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class NativeMiddlewareTest : BehaviorSpec({
    val classUnderTest = NativeMiddleware()

    context("Resolving the ServiceLocation") {
        withEnvironment("SDV_TESTSERVICE_ADDRESS", "localhost:12345") {
            `when`("Trying to resolve a well-defined service") {
                val lcServiceLocation = classUnderTest.getServiceLocation("testservice")
                val ucServiceLocation = classUnderTest.getServiceLocation("TESTSERVICE")

                then("It should resolve the correct host address, independent of the casing") {
                    lcServiceLocation shouldBe "localhost:12345"
                    lcServiceLocation shouldBe ucServiceLocation
                }
            }
        }

        `when`("Trying to resolve the default service for mqtt") {
            val lcServiceLocation = classUnderTest.getServiceLocation("mqtt")
            val ucServiceLocation = classUnderTest.getServiceLocation("MQTT")
            then("It should resolve the correct host address, independent of the casing") {
                lcServiceLocation shouldBe "localhost:1883"
                lcServiceLocation shouldBe ucServiceLocation
            }
        }

        `when`("Trying to resolve the default service for vehicledatabroker") {
            val lcServiceLocation = classUnderTest.getServiceLocation("vehicledatabroker")
            val ucServiceLocation = classUnderTest.getServiceLocation("VEHICLEDATABROKER")
            then("It should resolve the correct host address, independent of the casing") {
                lcServiceLocation shouldBe "localhost:55555"
                lcServiceLocation shouldBe ucServiceLocation
            }
        }

        `when`("Trying to resolve a Service defined by envVar with pure address") {
            withEnvironment(key = "SDV_SOMESERVICE_ADDRESS", value = "some-service-address") {
                val serviceLocation = classUnderTest.getServiceLocation("someservice")

                then("It should resolve to the content of the envVar") {
                    serviceLocation shouldBe "some-service-address"
                }
            }
        }

        `when`("Trying to resolve a Service defined by envVar with URL") {
            withEnvironment(key = "SDV_SOMESERVICE_ADDRESS", value = "scheme://some-host:port/path") {
                val serviceLocation = classUnderTest.getServiceLocation("someservice")

                then("It should resolve to the netLocation of the URL") {
                    serviceLocation shouldBe "some-host:port"
                }
            }
        }

        `when`("Trying to resolve an unknown service") {
            val result = runCatching {
                classUnderTest.getServiceLocation("unknownService")
            }
            then("It should throw an Exception") {
                result.isFailure shouldBe true
                result.exceptionOrNull() shouldNotBeNull { }
            }
        }

        `when`("Retrieving the MetaData of the NativeMiddleware") {
            val metaData = classUnderTest.getMetadata("someService")

            then("It returns an empty map") {
                metaData.isEmpty() shouldBe true
            }
        }

    }
})
