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

/**
 * Creates a Singleton [T] instance. Creation of the Singleton requires no arguments.
 */
abstract class NoArgumentSingletonHolder<out T>(private val constructor: () -> T) {

    @Volatile
    private var instance: T? = null

    /**
     * Returns the Singleton Instance.
     */
    fun getInstance(): T =
        instance ?: synchronized(this) {
            instance ?: constructor().also { instance = it }
        }
}

/**
 * Creates a Singleton [T] instance. Creation of the Singleton requires one argument of type [A].
 */
abstract class SingletonHolder<out T, in A>(private val constructor: (A) -> T) {

    @Volatile
    private var instance: T? = null

    /**
     * Returns the Singleton Instance.
     */
    fun getInstance(arg: A): T =
        instance ?: synchronized(this) {
            instance ?: constructor(arg).also { instance = it }
        }
}
