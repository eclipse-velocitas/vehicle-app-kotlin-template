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
import com.etas.e2e.tools.byd.DoorServiceGrpcKt
import com.etas.e2e.tools.byd.getLockStatusRequest
import com.etas.e2e.tools.byd.lockDoorRequest
import com.etas.e2e.tools.byd.openDoorRequest
import com.etas.e2e.tools.byd.unlockDoorRequest
import io.grpc.ChannelCredentials
import io.grpc.Grpc
import io.grpc.InsecureChannelCredentials
import io.grpc.ManagedChannel

private const val TAG = "SampleServiceImpl"

/**
 * A example implementation of a VehicleService providing Vehicle Functionality. See proto/DoorService.proto
 */
class VehicleService(
    private val host: String,
    private val port: Int,
    private val channelCredentials: ChannelCredentials = InsecureChannelCredentials.create()
) {
    private var managedChannel: ManagedChannel? = null
    private lateinit var doorService: DoorServiceGrpcKt.DoorServiceCoroutineStub

    /**
     * Connects to the VehicleService. Once connected it is possible to execute the corresponding Service Operations
     */
    fun connect() {
        managedChannel = Grpc.newChannelBuilderForAddress(host, port, channelCredentials)
            .build()
            .also {
                doorService = DoorServiceGrpcKt.DoorServiceCoroutineStub(it)
            }
    }

    /**
     * Disconnects from the VehicleService. Once disconnected it is no longer possible to execute the corresponding
     * Service Operations.
     */
    fun disconnect() {
        managedChannel?.shutdown()
        managedChannel = null
    }

    /**
     * Locks the vehicle doors.
     *
     * @throws IllegalStateException when the DoorService is not connected. Call #connect first!
     */
    suspend fun lockDoor(): Boolean {
        check(::doorService.isInitialized) { "Not connected to DoorService. Call #connect first." }

        val request = lockDoorRequest { }
        val response = doorService.lockDoor(request)
        Log.i(TAG, "lockDoor: Got response: " + response.success)
        return response.success
    }

    /**
     * Unlocks the vehicle doors.
     *
     * @throws IllegalStateException when the DoorService is not connected. Call #connect first!
     */
    suspend fun unlockDoor(): Boolean {
        check(::doorService.isInitialized) { "Not connected to DoorService. Call #connect first." }

        val request = unlockDoorRequest { }
        val response = doorService.unlockDoor(request)
        Log.i(TAG, "unlockDoor: Got response: " + response.success)
        return response.success
    }

    /**
     * Opens the vehicle doors.
     *
     * @throws IllegalStateException when the DoorService is not connected. Call #connect first!
     */
    suspend fun openDoor(): Long {
        check(::doorService.isInitialized) { "Not connected to DoorService. Call #connect first." }

        val request = openDoorRequest { }
        val response = doorService.openDoor(request)
        return response.duration
    }

    /**
     * Retrieves the lock status of the vehicle doors.
     *
     * @throws IllegalStateException when the DoorService is not connected. Call #connect first!
     */
    suspend fun fetchLockStatus(): Int {
        check(::doorService.isInitialized) { "Not connected to DoorService. Call #connect first." }

        val request = getLockStatusRequest { }
        val response = doorService.getLockStatus(request)
        return response.status
    }
}
