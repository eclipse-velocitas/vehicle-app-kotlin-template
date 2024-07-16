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
import velocitas.sdk.logging.VelocitasLogger

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
class SingleResponseGrpcCall<TRequestType, TReplyType> : GrpcCall() {
    /**
     * The request of the GrpcCall.
     */
    var request: TRequestType? = null

    /**
     * The Response of the GrpcCall.
     */
    var reply: TReplyType? = null
}

/**
 * A GRPC call where a request is followed up by multiple streamed replies.
 *
 * @param TRequestType The data type of the request.
 * @param TReplyType   The data type of the reply.
 */
class StreamingResponseGrpcCall<TRequestType, TReplyType> : GrpcCall() {
    /**
     * The request of the GrpcCall.
     */
    var request: TRequestType? = null
        private set

    private val readReactor: ReadReactor<TRequestType, TReplyType> = ReadReactor(this)

    /**
     * Executes the GrpcCall. Sends the request and listens for updates.
     */
    fun startCall(): StreamingResponseGrpcCall<TRequestType, TReplyType> {
        // TODO HOW TO IMPLEMENT THIS?
        return this
    }

    /**
     * Sets the [handler] to whom the replies received by the stream are sent.
     */
    fun onData(handler: ((TReplyType) -> Unit)): StreamingResponseGrpcCall<TRequestType, TReplyType> {
        readReactor.onDataHandler = handler
        return this
    }

    /**
     * Sets the [handler] which should be notified about the stream reaching it's end.
     */
    fun onFinish(handler: (() -> Unit)): StreamingResponseGrpcCall<TRequestType, TReplyType> {
        readReactor.onFinishHandler = handler
        return this
    }

    private class ReadReactor<TRequestType, TReplyType>(
        private val parent: StreamingResponseGrpcCall<TRequestType, TReplyType>,
    ) : StreamObserver<TReplyType> {
        var reply: TReplyType? = null
        var onDataHandler: ((TReplyType) -> Unit)? = null
        var onFinishHandler: (() -> Unit)? = null

        @Suppress("TooGenericExceptionCaught") // executed code could throw any exception
        override fun onNext(value: TReplyType) {
            this.reply = value
            try {
                onDataHandler?.invoke(value)
            } catch (e: Exception) {
                VelocitasLogger.error("${e.message}")
            }
        }

        override fun onError(t: Throwable) {
            VelocitasLogger.error("${t.message}")
        }

        override fun onCompleted() {
            onFinishHandler?.invoke()
            parent.isComplete = true
        }
    }
}
