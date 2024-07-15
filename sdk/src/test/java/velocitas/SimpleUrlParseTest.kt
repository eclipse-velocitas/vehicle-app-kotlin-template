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

package velocitas

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class SimpleUrlParseTest : BehaviorSpec({
    context("Parsing NetLocation and Scheme") {
        `when`("Parsing 'http://username:password@hostname:portnumber/path'") {
            val scheme = "http"
            val netLocation = "username:password@hostname:portnumber"
            val url = "$scheme://$netLocation/path"

            val result = SimpleUrlParse(url)

            then("The scheme should be resolved to 'http'") {
                result.scheme shouldBe scheme
            }
            then("The netLocation should be resolved to 'username:password@hostname:portnumber'") {
                result.netLocation shouldBe netLocation
            }
        }

        `when`("Parsing 'http://somehost:1234/somePath'") {
            val scheme = "http"
            val netLocation = "somehost:1234"
            val url = "$scheme://$netLocation/somePath"

            val result = SimpleUrlParse(url)

            then("The scheme should be resolved to 'http'") {
                result.scheme shouldBe scheme
            }
            then("The netLocation should be resolved to 'username:password@hostname:portnumber'") {
                result.netLocation shouldBe netLocation
            }
        }

        `when`("Parsing 'HTTP://username:password@hostname:portnumber/path'") {
            val scheme = "HTTP"
            val netLocation = "username:password@hostname:portnumber"
            val url = "$scheme://$netLocation/path"

            val result = SimpleUrlParse(url)

            then("The scheme should be resolved in lowercase 'username:password@hostname:portnumber'") {
                scheme.lowercase() shouldBe result.scheme
            }
        }

        `when`("Parsing 'mailto:receipient@somewhere.io'") {
            val url = "mailto:receipient@somewhere.io"

            val result = SimpleUrlParse(url)

            then("The scheme will be resolved to an empty String") {
                result.scheme shouldBe ""
            }
            then("The netLocation should be resolved to 'mailto:receipient@somewhere.io'") {
                result.netLocation shouldBe url
            }
        }

        `when`("Parsing '//127.0.0.1:42/somePath'") {
            val netLocation = "127.0.0.1:42"
            val url = "//$netLocation/somePath"

            val result = SimpleUrlParse(url)

            then("The scheme should be resolved to an empty String") {
                result.scheme shouldBe ""
            }
            then("The netLocation should be resolved to '127.0.0.1:42'") {
                result.netLocation shouldBe netLocation
            }
        }

        `when`("Parsing 'localhost:123/somePath'") {
            val netLocation = "localhost:123"
            val url = "$netLocation/somePath"

            val result = SimpleUrlParse(url)

            then("The scheme should be resolved to an empty String") {
                result.scheme shouldBe ""
            }
            then("The netLocation should be resolved to 'localhost:1234'") {
                result.netLocation shouldBe netLocation
            }
        }
    }
})
