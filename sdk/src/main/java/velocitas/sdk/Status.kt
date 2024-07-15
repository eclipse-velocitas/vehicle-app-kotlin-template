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
 * Status of an asynchronous request.
 *
 * @constructor Construct a new Status object without any errors.
 */
class Status() {
    /**
     * Returns whether the reported status is OK or not.
     *
     * @return true   Status is OK
     * @return false  Status is not OK
     */
    var isOk = true
        private set

    /**
     * Returns whether the reported status is OK or not.
     *
     * @return true   Status is OK
     * @return false  Status is not OK
     */
    var errorMessage: String? = null
        private set

    /**
     * Construct a new error status.
     *
     * @param errorMsg
     */
    constructor(errorMsg: String) : this() {
        this.isOk = false
        this.errorMessage = errorMsg
    }
}
