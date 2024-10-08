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

import android.util.Log
import door.DoorGrpc
import door.DoorGrpc.DoorFutureStub
import door.DoorService
import io.grpc.Grpc
import io.grpc.InsecureChannelCredentials

private const val TAG = "SampleServiceImpl"

class GrpcCarService(
    host: String,
    port: Int,
) : CarService {

    private val doorService: DoorFutureStub

    init {
        Log.i(TAG, "Connecting to gRPC service at $host:$port")

        val channelCredentials = InsecureChannelCredentials.create()
        val channel = Grpc.newChannelBuilderForAddress(host, port, channelCredentials).build()

        doorService = DoorGrpc.newFutureStub(channel)
    }

    // Door service
    override fun lockDoor(): Boolean {
        val request = DoorService.LockRequest.newBuilder().build()
        val response = doorService.lock(request).get() // blocking call
        Log.i(TAG, "lockDoor: Got response: " + response.getCode())
        return response.getCode() == DoorService.BCMReturnCode.BCM_RETURN_CODE_SUCCESS
    }

    override fun unlockDoor(): Boolean {
        val request = DoorService.UnlockRequest.newBuilder().build()
        val response = doorService.unlock(request).get() // blocking call
        Log.i(TAG, "unlockDoor: Got response: " + response.getCode())
        return response.getCode() == DoorService.BCMReturnCode.BCM_RETURN_CODE_SUCCESS
    }
}
