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

package com.example.logger

import android.util.Log
import org.eclipse.velocitas.sdk.logging.LoggingStrategy

/**
 * LoggingStrategy to output all logs using Android Logcat.
 */
object LogcatLoggingStrategy : LoggingStrategy {
    override fun info(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun warn(tag: String, message: String) {
        Log.w(tag, message)
    }

    override fun error(tag: String, message: String) {
        Log.e(tag, message)
    }

    override fun debug(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun verbose(tag: String, message: String) {
        Log.v(tag, message)
    }
}
