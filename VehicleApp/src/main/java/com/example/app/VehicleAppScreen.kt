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

import kotlin.random.Random
import kotlinx.coroutines.launch
import androidx.car.app.CarContext
import androidx.car.app.CarToast
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
                runCatching {
                    dataBrokerService.fetchSpeed()
                }.onSuccess { vehicleSpeed ->
                    Logger.info(TAG, "Vehicle.Speed = $vehicleSpeed")
                    showToast(carContext, "Vehicle.Speed = $vehicleSpeed")
                }.onFailure { throwable ->
                    Logger.warn(TAG, "An error occurred: ", throwable)
                }
            }
        }

        val row2 = createRow(playIcon, "Update Vehicle.Speed") {
            lifecycleScope.launch {
                val random = Random(System.nanoTime())
                val newSpeedValue = random.nextInt(120).toFloat()
                Logger.info(TAG, "Update Vehicle.Speed to $newSpeedValue")

                runCatching {
                    dataBrokerService.updateSpeed(newSpeedValue)
                }.onSuccess {
                    Logger.info(TAG, "Vehicle.Speed updated successfully")
                    showToast(carContext, "Vehicle.Speed updated successfully")
                }.onFailure { throwable ->
                    Logger.warn(TAG, "An error occurred: ", throwable)
                }
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
                runCatching {
                    vehicleApp.vehicleService.lockDoor()
                }.onSuccess { success ->
                    if (success) {
                        Logger.debug(TAG, "Doors locked successfully")
                        showToast(carContext, "Doors locked successfully")
                    }
                }.onFailure { throwable ->
                    Logger.warn(TAG, "An error occurred: ", throwable)
                }
            }
        }

        val row5 = createRow(playIcon, "Unlock Doors") {
            lifecycleScope.launch {
                Logger.debug(TAG, "Requesting to unlock the doors")
                runCatching {
                    vehicleApp.vehicleService.unlockDoor()
                }.onSuccess { success ->
                    if (success) {
                        Logger.debug(TAG, "Doors unlocked successfully")
                        showToast(carContext, "Doors unlocked successfully")
                    }
                }.onFailure { throwable ->
                    Logger.warn(TAG, "An error occurred: ", throwable)
                }
            }
        }

        val row6 = createRow(playIcon, "Get Lock Status") {
            lifecycleScope.launch {
                Logger.debug(TAG, "Requesting Door Lock Status")
                runCatching {
                    vehicleApp.vehicleService.fetchLockStatus()
                }.onSuccess { status ->
                    Logger.debug(TAG, "Door Lock Status: $status")
                    showToast(carContext, "Door Lock Status: $status")
                }.onFailure { throwable ->
                    Logger.warn(TAG, "An error occurred: ", throwable)
                }
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
        CarToast.makeText(
            context,
            message,
            CarToast.LENGTH_SHORT
        ).show()
    }
}
