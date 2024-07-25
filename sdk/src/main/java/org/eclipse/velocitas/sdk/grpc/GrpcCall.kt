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

package org.eclipse.velocitas.sdk.grpc

import io.grpc.stub.StreamObserver
import org.eclipse.velocitas.sdk.logging.Logger

abstract class GrpcCall {
    // grpc::ClientContext context
    var isComplete = false
}

/**
 * A GRPC call where a request is followed up by a single reply.
 *
 * @param TRequestType The data type of the request.
 * @param TResponseType The data type of the response.
 */
class SingleResponseGrpcCall<TRequestType, TResponseType>(
    /**
     * The request of the GrpcCall.
     */
    var request: TRequestType,
) : GrpcCall() {

    /**
     * The Response of the GrpcCall.
     */
    var response: TResponseType? = null
}

/**
 * A GRPC call where a request is followed up by multiple streamed replies.
 *
 * @param TRequestType    The data type of the request.
 * @param TResponseType   The data type of the reply.
 */
class StreamingResponseGrpcCall<TRequestType, TResponseType>(
    /**
     * The request of the GrpcCall.
     */
    val request: TRequestType? = null,
) : GrpcCall() {
    var lastResponse: TResponseType? = null
}

/**
 * A GRPC call where a request is followed up by multiple streamed replies.
 *
 * @param TRequestType    The data type of the request.
 * @param TResponseType   The data type of the reply.
 */
class BidiStreamingResponseGrpcCall<TRequestType, TResponseType> : GrpcCall() {
    var lastResponse: TResponseType? = null
}

class AsyncGrpcObserver<TResponseType> : StreamObserver<TResponseType> {
    var response: TResponseType? = null
    var onResponseHandler: ((TResponseType) -> Unit)? = null
    var onErrorHandler: ((Throwable) -> Unit)? = null
    var onFinishHandler: (() -> Unit)? = null

    @Suppress("TooGenericExceptionCaught") // executed code could throw any exception
    override fun onNext(value: TResponseType) {
        this.response = value
        try {
            onResponseHandler?.invoke(value)
        } catch (e: Exception) {
            Logger.error("${e.message}")
            onErrorHandler?.invoke(e)
        }
    }

    override fun onError(t: Throwable) {
        Logger.error("${t.message}")
        onErrorHandler?.invoke(t)
    }

    override fun onCompleted() {
        onFinishHandler?.invoke()
    }
}
