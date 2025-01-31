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

package com.example.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import com.example.logger.LogcatLoggingStrategy
import com.example.service.DataBrokerModelService
import com.example.service.DataBrokerV1Service
import com.example.service.DataBrokerV2Service
import com.example.service.VehicleService
import org.eclipse.velocitas.sdk.VehicleApplication
import org.eclipse.velocitas.sdk.logging.Logger

private const val DATABROKER_HOST = "10.0.2.2"
private const val DATABROKER_PORT = 55556

private const val VEHICLE_SERVICE_HOST = "10.0.2.2"
private const val VEHICLE_SERVICE_PORT = 12345

private const val TAG = "VehicleApp"

class VehicleApp : VehicleApplication() {
    private val supervisorJob = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + supervisorJob)

    val dataBrokerV1Service = DataBrokerV1Service(DATABROKER_HOST, DATABROKER_PORT)
    val dataBrokerV2Service = DataBrokerV2Service(DATABROKER_HOST, DATABROKER_PORT)
    val dataBrokerModelService = DataBrokerModelService(DATABROKER_HOST, DATABROKER_PORT)

    val vehicleService: VehicleService = VehicleService(VEHICLE_SERVICE_HOST, VEHICLE_SERVICE_PORT)

    init {
        Logger.loggingStrategy = LogcatLoggingStrategy
    }

    override fun onStart() {
        coroutineScope.launch {
            Logger.info(TAG, "DataBrokerV1Service connecting at $DATABROKER_HOST:$DATABROKER_PORT...")
            dataBrokerV1Service.connect()
            Logger.info(TAG, "DataBrokerV1Service Connection established")
        }
        coroutineScope.launch {
            Logger.info(TAG, "DataBrokerV2Service connecting at $DATABROKER_HOST:$DATABROKER_PORT...")
            dataBrokerV2Service.connect()
            Logger.info(TAG, "DataBrokerV2Service Connection established")
        }
        coroutineScope.launch {
            Logger.info(TAG, "DataBrokerModelService connecting at $DATABROKER_HOST:$DATABROKER_PORT...")
            dataBrokerModelService.connect()
            Logger.info(TAG, "DataBrokerModelService Connection established")
        }
        coroutineScope.launch {
            Logger.info(TAG, "Connecting to VehicleService at $VEHICLE_SERVICE_HOST:$VEHICLE_SERVICE_PORT...")
            vehicleService.connect()
            Logger.info(TAG, "Connection to VehicleService established")
        }
    }

    override fun onStop() {
        dataBrokerV1Service.disconnect()
        dataBrokerV2Service.disconnect()
        vehicleService.disconnect()

        supervisorJob.cancelChildren()
    }
}
