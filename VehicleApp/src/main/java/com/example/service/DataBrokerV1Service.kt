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

import io.grpc.ChannelCredentials
import io.grpc.Grpc
import io.grpc.InsecureChannelCredentials
import org.eclipse.kuksa.connectivity.databroker.DataBrokerException
import org.eclipse.kuksa.connectivity.databroker.v1.DataBrokerConnection
import org.eclipse.kuksa.connectivity.databroker.v1.DataBrokerConnector
import org.eclipse.kuksa.connectivity.databroker.v1.listener.VssPathListener
import org.eclipse.kuksa.connectivity.databroker.v1.request.FetchRequest
import org.eclipse.kuksa.connectivity.databroker.v1.request.SubscribeRequest
import org.eclipse.kuksa.connectivity.databroker.v1.request.UpdateRequest
import org.eclipse.kuksa.proto.v1.KuksaValV1
import org.eclipse.kuksa.proto.v1.datapoint

private const val VSS_VEHICLE_SPEED = "Vehicle.Speed"

/**
 * DataBrokerService which uses the kuksa.val.v1 protocol.
 */
class DataBrokerV1Service(
    private val host: String,
    private val port: Int,
    private val channelCredentials: ChannelCredentials = InsecureChannelCredentials.create(),
) : DataBrokerService {
    private lateinit var dataBrokerConnection: DataBrokerConnection

    override suspend fun connect() {
        val managedChannel = Grpc.newChannelBuilderForAddress(host, port, channelCredentials).build()

        DataBrokerConnector(managedChannel).apply {
            dataBrokerConnection = connect()
        }
    }

    override fun disconnect() {
        dataBrokerConnection.disconnect()
    }

    override suspend fun fetchSpeed(): Float {
        check(::dataBrokerConnection.isInitialized) { "Not connected to Databroker. Call #connect first." }

        val request = FetchRequest(VSS_VEHICLE_SPEED)
        val response = dataBrokerConnection.fetch(request)

        val entriesList = response.entriesList
        val dataEntry = entriesList.first()
        return dataEntry.value.float
    }

    override suspend fun updateSpeed(speed: Float): Boolean {
        check(::dataBrokerConnection.isInitialized) { "Not connected to Databroker. Call #connect first." }

        val datapoint = datapoint { float = speed }
        val request = UpdateRequest(VSS_VEHICLE_SPEED, datapoint)

        try {
            dataBrokerConnection.update(request)
        } catch (e: DataBrokerException) {
            return false
        }
        return true
    }

    override suspend fun subscribeSpeed(
        onUpdate: (Float) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        check(::dataBrokerConnection.isInitialized) { "Not connected to Databroker. Call #connect first." }

        val request = SubscribeRequest(VSS_VEHICLE_SPEED)
        return dataBrokerConnection.subscribe(request, object : VssPathListener {
            override fun onEntryChanged(entryUpdates: List<KuksaValV1.EntryUpdate>) {
                entryUpdates.forEach { entryUpdate ->
                    val speedValue = entryUpdate.entry.value.float

                    onUpdate(speedValue)
                }
            }

            override fun onError(throwable: Throwable) {
                onError(throwable)
            }
        })
    }

}
