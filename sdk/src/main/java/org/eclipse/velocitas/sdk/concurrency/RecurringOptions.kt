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

package org.eclipse.velocitas.sdk.concurrency

import java.util.concurrent.TimeUnit

/**
 * RecurringOptions allow a Job to be executed multiple times. The executions will commence after [initialDelay] then
 * [initialDelay] + [period], then [initialDelay] + 2 * [period], and so on.
 */
data class RecurringOptions(
    /**
     * The initial delay specified by [unit] before the task is executed the first time.
     */
    val initialDelay: Long,

    /**
     * The period of time between the task executed again.
     */
    val period: Long,

    /**
     * The unit in which [initialDelay] and [period] are provided.
     */
    val unit: TimeUnit,
)
