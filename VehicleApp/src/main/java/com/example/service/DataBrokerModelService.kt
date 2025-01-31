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
import org.eclipse.kuksa.connectivity.databroker.v1.listener.VssNodeListener
import org.eclipse.kuksa.connectivity.databroker.v1.request.VssNodeFetchRequest
import org.eclipse.kuksa.connectivity.databroker.v1.request.VssNodeSubscribeRequest
import org.eclipse.kuksa.connectivity.databroker.v1.request.VssNodeUpdateRequest
import org.eclipse.velocitas.vss.VssVehicle

/**
 * DataBrokerService which uses the kuksa.val.v1 protocol with generated VSS Models.
 */
class DataBrokerModelService(
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

        val vssNode = VssVehicle.VssSpeed()
        val request = VssNodeFetchRequest(vssNode)

        val vssSpeed = dataBrokerConnection.fetch(request)
        return vssSpeed.value
    }

    override suspend fun updateSpeed(speed: Float): Boolean {
        check(::dataBrokerConnection.isInitialized) { "Not connected to Databroker. Call #connect first." }

        val vssNode = VssVehicle.VssSpeed(speed)
        val request = VssNodeUpdateRequest(vssNode)
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

        val vssNode = VssVehicle.VssSpeed()
        val request = VssNodeSubscribeRequest(vssNode)
        dataBrokerConnection.subscribe(request, object : VssNodeListener<VssVehicle.VssSpeed> {
            override fun onError(throwable: Throwable) {
                onError(throwable)
            }

            override fun onNodeChanged(vssNode: VssVehicle.VssSpeed) {
                val speedValue = vssNode.value
                onUpdate(speedValue)
            }
        })
    }
}
