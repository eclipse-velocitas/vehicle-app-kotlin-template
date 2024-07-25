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

/**
 * Base exception when there is an issue during remote procedure calls.
 */
class RpcException(message: String, throwable: Throwable? = null) : RuntimeException(message, throwable)

/**
 * Base exception when a type cannot be converted.
 */
class InvalidTypeException(message: String, throwable: Throwable? = null) : RuntimeException(message, throwable)

/**
 * Base exception when an invalid value is received.
 */
class InvalidValueException(message: String, throwable: Throwable? = null) : RuntimeException(message, throwable)

/**
 * Any issue which occurred during async invocation.
 */
class AsyncException(message: String, throwable: Throwable? = null) : RuntimeException(message, throwable)
