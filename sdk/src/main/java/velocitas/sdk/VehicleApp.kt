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

package velocitas.sdk

import velocitas.sdk.logging.VelocitasLogger
import velocitas.sdk.middleware.Middleware

/**
 * Base class for all vehicle apps which manages an app's lifecycle.
 */
abstract class VehicleApp {
    /**
     * Runs the Vehicle App.
     */
    fun run() {
        VelocitasLogger.info("Running App...")
        Middleware.getInstance().start()
        Middleware.getInstance().waitUntilReady()

        onStart()

        // TODO: Fix busy waiting
        while (true) {
            Thread.sleep(1)
        }
    }

    /**
     * Stops the Vehicle App
     */
    fun stop() {
        VelocitasLogger.info("Stopping App...")

        onStop()

        Middleware.getInstance().stop()
    }

    /**
     * Event which is called once the Vehicle App is started.
     */
    abstract fun onStart()

    /**
     * Event which is called once the Vehicle App is requested to stop.
     */
    abstract fun onStop()
}
