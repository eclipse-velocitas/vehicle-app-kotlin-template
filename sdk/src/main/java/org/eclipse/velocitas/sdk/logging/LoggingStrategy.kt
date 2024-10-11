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

package org.eclipse.velocitas.sdk.logging

/**
 * A generic logger interface. Can be used to switch the underlying logging strategy of the VelocitasLogger.
 */
interface LoggingStrategy {
    /**
     * Log a [message] with the specified [tag] on info level.
     */
    fun info(tag: String, message: String)

    /**
     * Log a [message] with the specified [tag] on warn level.
     */
    fun warn(tag: String, message: String)

    /**
     * Log a [message] with the specified [tag] on error level.
     */
    fun error(tag: String, message: String)

    /**
     * Log a [message] with the specified [tag] on debug level.
     */
    fun debug(tag: String, message: String)

    /**
     * Log a [message] with the specified [tag] on verbose level.
     */
    fun verbose(tag: String, message: String)
}
