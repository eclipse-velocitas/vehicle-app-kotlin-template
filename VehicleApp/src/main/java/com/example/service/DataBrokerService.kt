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

/**
 * DataBroker abstraction layer providing and demonstrating how to use the different protocols.
 *
 * Currently supported protocols:
 * - kuksa.val.v1 (incl. VSS Models; see: DataBrokerModelService)
 * - kuksa.val.v2
 */
interface DataBrokerService {
    /**
     * Connects to the DataBrokerService. Once connected it is possible to execute the corresponding Service Operations
     */
    suspend fun connect()

    /**
     * Disconnects from the DataBrokerService. Once disconnected it is no longer possible to execute the corresponding
     * Service Operations.
     */
    fun disconnect()

    /**
     * Fetches the Vehicle.Speed.
     */
    suspend fun fetchSpeed(): Float

    /**
     * Updates the Vehicle.Speed to the specified [speed].
     */
    suspend fun updateSpeed(speed: Float): Boolean

    /**
     * Subscribes to the Vehicle.Speed. Speed updates will be delivered using the [onUpdate] callback, while any error
     * occurring will be delivered using the [onError] callback.
     */
    suspend fun subscribeSpeed(
        onUpdate: (Float) -> Unit,
        onError: (Throwable) -> Unit,
    )
}
