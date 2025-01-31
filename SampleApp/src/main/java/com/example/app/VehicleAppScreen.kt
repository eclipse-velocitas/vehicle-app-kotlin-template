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

import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import com.example.service.DataBrokerService
import org.eclipse.velocitas.sdk.logging.Logger

private const val TAG = "VehicleAppScreen"

/**
 * The VehicleAppScreen to show information on the car infotainment system.
 *
 * @see Screen
 */
class VehicleAppScreen(carContext: CarContext, private val vehicleApp: VehicleApp) : Screen(carContext) {

    private var dataBrokerService: DataBrokerService = vehicleApp.dataBrokerV2Service

    override fun onGetTemplate(): Template {
        val rows = createRows(carContext)
        var paneBuilder = Pane.Builder()

        rows.forEach { row ->
            paneBuilder = paneBuilder.addRow(row)
        }

        return PaneTemplate.Builder(paneBuilder.build())
            .setHeaderAction(Action.APP_ICON)
            .build()
    }

    private fun createRows(carContext: CarContext): List<Row> {
        val iconCompat = IconCompat.createWithResource(
            carContext,
            R.drawable.baseline_play_circle_24,
        )
        val playIcon = CarIcon.Builder(iconCompat).build()

        val row0 = createRow(playIcon, "Using ${dataBrokerService::class.java.simpleName}") {
            when (dataBrokerService) {
                vehicleApp.dataBrokerV2Service -> {
                    dataBrokerService = vehicleApp.dataBrokerV1Service
                }
                vehicleApp.dataBrokerV1Service -> {
                    dataBrokerService = vehicleApp.dataBrokerModelService
                }
                vehicleApp.dataBrokerModelService -> {
                    dataBrokerService = vehicleApp.dataBrokerV2Service
                }
            }
            invalidate()
        }


        val row1 = createRow(playIcon, "Fetch Speed") {
            lifecycleScope.launch {
                Logger.info(TAG, "Requesting Vehicle.Speed")
                val vehicleSpeed = dataBrokerService.fetchSpeed()
                Logger.info(TAG, "Vehicle.Speed = $vehicleSpeed")
                showToast(carContext, "Vehicle.Speed = $vehicleSpeed")
            }
        }

        val row2 = createRow(playIcon, "Update Vehicle.Speed") {
            lifecycleScope.launch {
                Logger.info(TAG, "Update Vehicle.Speed to 120")
                dataBrokerService.updateSpeed(120.0F)

                Logger.info(TAG, "Vehicle.Speed updated successfully")
                showToast(carContext, "Vehicle.Speed updated successfully")
            }
        }

        val row3 = createRow(playIcon, "Subscribe Vehicle.Speed") {
            lifecycleScope.launch {

                dataBrokerService.subscribeSpeed(
                    onUpdate = { speedValue ->
                        Logger.info(TAG, "Subscription Update for Vehicle.Speed: $speedValue")
                    },
                    onError = { throwable ->
                        Logger.error(TAG, "An error occurred: ", throwable)
                    }
                )
                showToast(carContext, "Subscription to Vehicle.Speed successful")
            }
        }

        val row4 = createRow(playIcon, "Lock Doors") {
            lifecycleScope.launch {
                Logger.debug(TAG, "Requesting to lock the doors")
                val success = vehicleApp.vehicleService.lockDoor()
                if (success) {
                    Logger.debug(TAG, "Doors locked successfully")
                    showToast(carContext, "Doors locked successfully")
                }
            }
        }

        val row5 = createRow(playIcon, "Unlock Doors") {
            lifecycleScope.launch {
                Logger.debug(TAG, "Requesting to unlock the doors")
                val success = vehicleApp.vehicleService.unlockDoor()
                if (success) {
                    Logger.debug(TAG, "Doors unlocked successfully")
                    showToast(carContext, "Doors unlocked successfully")
                }
            }
        }

        val row6 = createRow(playIcon, "Get Lock Status") {
            lifecycleScope.launch {
                Logger.debug(TAG, "Requesting Door Lock Status")
                val doorLockStatus = vehicleApp.vehicleService.getLockStatus()
                Logger.debug(TAG, "Door Lock Status: $doorLockStatus")
                showToast(carContext, "Door Lock Status: $doorLockStatus")
            }
        }

        return arrayListOf(row0, row1, row2, row3, row4, row5, row6)
    }

    private fun createRow(
        icon: CarIcon,
        title: String,
        onClick: (() -> Unit)
    ): Row {
        val action = Action.Builder()
            .setTitle(title)
            .setIcon(icon)
            .setOnClickListener(onClick)
            .build()

        return Row.Builder()
            .setTitle(title)
            .addAction(action)
            .build()
    }

    private fun showToast(context: CarContext, message: String) {
        Toast.makeText(
            context.baseContext,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
