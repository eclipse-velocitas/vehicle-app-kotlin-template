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

package org.eclipse.velocitas.sdk

import org.eclipse.velocitas.sdk.logging.Logger
import org.eclipse.velocitas.sdk.middleware.MiddlewareFactory

/**
 * Base class for all vehicle apps which manages an app's lifecycle.
 */
abstract class VehicleApp {
    private val middlewareFactory = MiddlewareFactory()
    private val middleware = middlewareFactory.create()

    /**
     * Runs the Vehicle App.
     */
    fun run() {
        Logger.info("Running App...")
        middleware.start()
        middleware.waitUntilReady()

        onStart()
    }

    /**
     * Stops the Vehicle App
     */
    fun stop() {
        Logger.info("Stopping App...")

        onStop()

        middleware.stop()
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
