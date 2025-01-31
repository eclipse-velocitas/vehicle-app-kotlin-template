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
import org.eclipse.kuksa.connectivity.databroker.v2.DataBrokerConnectionV2
import org.eclipse.kuksa.connectivity.databroker.v2.DataBrokerConnectorV2
import org.eclipse.kuksa.connectivity.databroker.v2.request.FetchValueRequestV2
import org.eclipse.kuksa.connectivity.databroker.v2.request.PublishValueRequestV2
import org.eclipse.kuksa.connectivity.databroker.v2.request.SubscribeRequestV2
import org.eclipse.kuksa.proto.v2.Types
import org.eclipse.kuksa.proto.v2.Types.Datapoint
import org.eclipse.kuksa.proto.v2.Types.SignalID

private const val VSS_VEHICLE_SPEED = "Vehicle.Speed"

/**
 * DataBrokerService which uses the kuksa.val.v2 protocol.
 */
class DataBrokerV2Service(
    private val host: String,
    private val port: Int,
    private val channelCredentials: ChannelCredentials = InsecureChannelCredentials.create(),
) : DataBrokerService {
    private lateinit var dataBrokerConnection: DataBrokerConnectionV2

    override suspend fun connect() {
        val managedChannel = Grpc.newChannelBuilderForAddress(host, port, channelCredentials).build()

        DataBrokerConnectorV2(managedChannel).apply {
            dataBrokerConnection = connect()
        }
    }

    override fun disconnect() {
        dataBrokerConnection.disconnect()
    }

    override suspend fun fetchSpeed(): Float {
        check(::dataBrokerConnection.isInitialized) { "Not connected to Databroker. Call #connect first." }

        val signalId = SignalID.newBuilder().setPath(VSS_VEHICLE_SPEED).build()
        val request = FetchValueRequestV2(signalId)

        val response = dataBrokerConnection.fetchValue(request)
        return response.dataPoint.value.float
    }

    override suspend fun updateSpeed(speed: Float): Boolean {
        check(::dataBrokerConnection.isInitialized) { "Not connected to Databroker. Call #connect first." }

        val signalId = SignalID.newBuilder().setPath(VSS_VEHICLE_SPEED).build()
        val type = Types.Value.newBuilder().setFloat(speed).build()
        val datapoint = Datapoint.newBuilder().setValue(type).build()

        val request = PublishValueRequestV2(signalId, datapoint)

        try {
            dataBrokerConnection.publishValue(request)
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

        val signalPaths = listOf(VSS_VEHICLE_SPEED)
        val request = SubscribeRequestV2(signalPaths)

        try {
            val responseFlow = dataBrokerConnection.subscribe(request)
            responseFlow.collect {
                val datapoint = it.entriesMap[VSS_VEHICLE_SPEED] ?: return@collect
                val speedValue = datapoint.value.float

                onUpdate(speedValue)
            }
        } catch (e: DataBrokerException) {
            onError(e)
        }
    }

}
