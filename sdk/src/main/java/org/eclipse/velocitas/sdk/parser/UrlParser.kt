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

package org.eclipse.velocitas.sdk.parser

/**
 * Provides a simplified URL parser.
 *
 * It is able to parse just URL starting with a "://" behind the scheme
 * specifier, e.g. "http://somehost:1234/somePath", but not URLs like
 * "mailto:receipient@somewhere.io".
 * As an advantage it can handle "URLs" without leading scheme, like
 * "//127.0.0.1:42" or "localhost:123".
 * Currently it just provides access to the scheme and the network location
 * ("login") part of the URL.
 * Other elements to be added as needed ...
 */
class UrlParser {
    private val schemePartStart = "//"
    private val simplifiedSchemeSeparator = "://"

    /**
     * Parses the provided url and returns a [Result].
     */
    fun parse(url: String): Result {
        var scheme = ""
        var netLocation = ""

        var schemeLen = url.indexOf(simplifiedSchemeSeparator)
        if (schemeLen > -1) {
            scheme = url.substring(0, schemeLen).lowercase()
        } else {
            schemeLen = 0
        }

        var startOfSchemePart = url.indexOf(schemePartStart, schemeLen)
        if (startOfSchemePart > -1) {
            startOfSchemePart += schemePartStart.length
        } else {
            startOfSchemePart = 0
        }

        var netLocationLen = url.indexOf("/", startOfSchemePart)
        if (netLocationLen == -1) {
            netLocationLen = url.length
        }
        netLocation = url.substring(startOfSchemePart, netLocationLen)

        return Result(scheme, netLocation)
    }

    data class Result(
        /**
         * The parsed scheme which can be the empty string if the URL does not contain a scheme
         */
        val scheme: String = "",

        /**
         * The network location part of the parsed URL
         *
         * This is the part between the leading double slashes and the first slash after,
         * e.g. URL = "http://username:password@hostname:portnumber/path"
         * --> netLocation = "username:password@hostname:portnumber"
         */
        val netLocation: String = "",
    )
}
