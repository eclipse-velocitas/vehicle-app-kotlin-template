package velocitas.sdk.grpc

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe

class GrpcClientTest : BehaviorSpec({
    given("A GrpcClient") {
        val grpcClient = GrpcClient()

        `when`("An active call is added") {
            val call = GrpcSingleResponseCall<String, String>()
            grpcClient.addActiveCall(call)

            then ("There should be one active call") {
                grpcClient.numActiveCalls shouldBe 1
            }
        }

        `when`("A second active call is added") {
            val call = GrpcSingleResponseCall<String, String>()
            grpcClient.addActiveCall(call)

            then("There should be two active calls") {
                grpcClient.numActiveCalls shouldBe 2
            }

            and("When one of the active calls finishes") {
                call.isComplete = true

                then("It should be pruned from the active calls") {
                    grpcClient.numActiveCalls shouldBe 1
                }
            }
        }
    }
})
