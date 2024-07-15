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

package velocitas.sdk.grpc

import io.kotest.core.spec.style.BehaviorSpec
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
