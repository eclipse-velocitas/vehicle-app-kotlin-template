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

package com.example.service

import DoorServiceGrpc
import DoorServiceOuterClass
import android.util.Log
import io.grpc.Grpc
import io.grpc.InsecureChannelCredentials

private const val TAG = "SampleServiceImpl"

class GrpcCarService(
    host: String,
    port: Int,
) : CarService {

    private val doorService: DoorServiceGrpc.DoorServiceFutureStub

    init {
        Log.i(TAG, "Connecting to gRPC service at $host:$port")

        val channelCredentials = InsecureChannelCredentials.create()
        val channel = Grpc.newChannelBuilderForAddress(host, port, channelCredentials).build()

        doorService = DoorServiceGrpc.newFutureStub(channel)
    }

    // Door service
    override fun lockDoor(): Boolean {
        val request = DoorServiceOuterClass.LockDoorRequest.newBuilder().build()
        val response = doorService.lockDoor(request).get() // blocking call
        Log.i(TAG, "lockDoor: Got response: " + response.success)
        return response.success
    }

    override fun unlockDoor(): Boolean {
        val request = DoorServiceOuterClass.UnlockDoorRequest.newBuilder().build()
        val response = doorService.unlockDoor(request).get() // blocking call
        Log.i(TAG, "unlockDoor: Got response: " + response.success)
        return response.success
    }
}
