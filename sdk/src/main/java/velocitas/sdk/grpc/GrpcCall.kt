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

package velocitas.sdk.grpc

import io.grpc.stub.StreamObserver
import velocitas.sdk.Logger

abstract class GrpcCall {
    // grpc::ClientContext context
    var isComplete = false
}

/**
 * A GRPC call where a request is followed up by a single reply.
 *
 * @param TRequestType The data type of the request.
 * @param TReplyType   The data type of the reply.
 */
class GrpcSingleResponseCall<TRequestType, TReplyType> : GrpcCall() {
    var request: TRequestType? = null
    var reply: TReplyType? = null
}

class GrpcStreamingResponseCall<TRequestType, TReplyType> : GrpcCall() {

    var request: TRequestType? = null
        private set

    private val readReactor: ReadReactor<TRequestType, TReplyType> = ReadReactor(this)

    fun startCall(): GrpcStreamingResponseCall<TRequestType, TReplyType> {
        // readReactor.startCall()
        // readReactor.startRead(readReactor.reply!!) // TODO check this
        return this
    }

    fun onData(handler: ((TReplyType) -> Unit)): GrpcStreamingResponseCall<TRequestType, TReplyType> {
        readReactor.onDataHandler = handler
        return this
    }

    fun onFinish(handler: (() -> Unit)): GrpcStreamingResponseCall<TRequestType, TReplyType> {
        readReactor.onFinishHandler = handler
        return this
    }

    class ReadReactor<TRequestType, TReplyType>(
        private val parent: GrpcStreamingResponseCall<TRequestType, TReplyType>
    ) : StreamObserver<TReplyType> {
        var reply: TReplyType? = null
        var onDataHandler: ((TReplyType) -> Unit)? = null
        var onFinishHandler: (() -> Unit)? = null

        override fun onNext(value: TReplyType) {
            this.reply = value
            try {
                onDataHandler?.invoke(value)
            } catch (e: Exception) {
                Logger.error("${e.message}")
            }
        }

        override fun onError(t: Throwable) {
            Logger.error("${t.message}")
        }

        override fun onCompleted() {
            onFinishHandler?.invoke()
            parent.isComplete = true
        }
    }
}
