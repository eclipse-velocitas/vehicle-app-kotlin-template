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

import com.google.common.util.concurrent.ListenableFuture
import io.grpc.Channel
import io.grpc.Context
import io.grpc.stub.StreamObserver
import org.eclipse.kuksa.proto.v2.KuksaValV2.*
import org.eclipse.kuksa.proto.v2.Types.Datapoint
import org.eclipse.kuksa.proto.v2.Types.SignalID
import org.eclipse.kuksa.proto.v2.VALGrpc

/**
 * AsyncBrokerGrpcFacade provides asynchronous communication against the VehicleDataBroker.
 * The current implementation uses the 'kuksa.val.v2' protocol which can be found here:
 * https://github.com/eclipse-kuksa/kuksa-databroker/tree/main/proto/kuksa/val/v2.
 */
class BrokerGrpcFacade(private val channel: Channel) : GrpcClient {
    private val asyncStub: VALGrpc.VALStub
        get() = VALGrpc.newStub(channel)

    private val futureStub: VALGrpc.VALFutureStub
        get() = VALGrpc.newFutureStub(channel)

    /**
     * Gets the latest value of a [signalId].
     *
     * The server might respond with the following GRPC error codes:
     *    NOT_FOUND if the requested signal doesn't exist
     *    PERMISSION_DENIED if access is denied
     */
    fun getValue(signalId: SignalID): ListenableFuture<GetValueResponse> {
        val request = GetValueRequest.newBuilder()
            .setSignalId(signalId)
            .build()

        return futureStub.getValue(request)
    }

    /**
     * Gets the latest values of a set of [signalIds]. The returned list of data points has the same order as the list
     * of the request.
     *
     * The server might respond with the following GRPC error codes:
     *    NOT_FOUND if any of the requested signals doesn't exist.
     *    PERMISSION_DENIED if access is denied for any of the requested signals.
     */
    fun getValues(signalIds: List<SignalID>): ListenableFuture<GetValuesResponse> {
        val request = GetValuesRequest.newBuilder()
            .addAllSignalIds(signalIds)
            .build()

        return futureStub.getValues(request)
    }

    /**
     * Lists the values of [signalIds] matching the request and responds with a list of signal values. Only values of
     * signals that the user is allowed to read are included (everything else is ignored).
     *
     * The server might respond with the following GRPC error codes:
     *    NOT_FOUND if any of the requested signals doesn't exist.
     *    PERMISSION_DENIED if access is denied for any of the requested signals.
     */
    fun listValues(signalIds: List<SignalID>): ListenableFuture<ListValuesResponse> {
        val request = ListValuesRequest.newBuilder()
            .addAllSignalIds(signalIds)
            .build()

        return futureStub.listValues(request)
    }

    /**
     * Subscribes to a set of [signalIds].
     * Returns a cancellableContext to cancel the active subscriptions.
     *
     * The server might respond with the following GRPC error codes:
     *    NOT_FOUND if any of the signals are non-existent.
     *    PERMISSION_DENIED if access is denied for any of the signals.
     */
    fun subscribe(
        signalIds: List<SignalID>,
        streamObserver: StreamObserver<SubscribeResponse>,
    ): Context.CancellableContext {
        val request = SubscribeRequest.newBuilder()
            .addAllSignalIds(signalIds)
            .build()

        val currentContext = Context.current()
        val cancellableContext = currentContext.withCancellation()
        cancellableContext.run {
            asyncStub.subscribe(request, streamObserver)
        }

        return cancellableContext
    }

    /**
     * Actuates a single actuator with the specified [signalId].
     *
     * The server might respond with the following GRPC error codes:
     *    NOT_FOUND if the actuator does not exist.
     *    PERMISSION_DENIED if access is denied for of the actuator.
     *    UNAVAILABLE if there is no provider currently providing the actuator
     *    INVALID_ARGUMENT
     *        - if the data type used in the request does not match the data type of the addressed signal
     *        - if the requested value is not accepted, e.g. if sending an unsupported enum value
     */
    fun actuate(signalId: SignalID): ListenableFuture<ActuateResponse> {
        val request = ActuateRequest.newBuilder()
            .setSignalId(signalId)
            .build()

        return futureStub.actuate(request)
    }

    /**
     * Actuates simultaneously multiple actuators with the specified [signalIds].
     * If any error occurs, the entire operation will be aborted and no single actuator value will be forwarded to the
     * provider.
     *
     * The server might respond with the following GRPC error codes:
     *     NOT_FOUND if any of the actuators are non-existent.
     *     PERMISSION_DENIED if access is denied for any of the actuators.
     *     UNAVAILABLE if there is no provider currently providing an actuator
     *     INVALID_ARGUMENT
     *         - if the data type used in the request does not match the data type of the addressed signal
     *         - if the requested value is not accepted, e.g. if sending an unsupported enum value
     *
     */
    fun batchActuate(signalIds: List<SignalID>): ListenableFuture<BatchActuateResponse> {
        val requestBuilder = BatchActuateRequest.newBuilder()
        signalIds.forEach { signalId ->
            val actuateRequest = ActuateRequest.newBuilder().setSignalId(signalId)
            requestBuilder.addActuateRequests(actuateRequest)
        }
        val request = requestBuilder.build()

        return futureStub.batchActuate(request)
    }

    /**
     * Lists metadata of signals matching the request.
     * If any error occurs, the entire operation will be aborted and no single actuator value will be forwarded to the
     * provider.
     *
     * The server might respond with the following GRPC error codes:
     *     NOT_FOUND if the specified root branch does not exist.
     */
    fun listMetadata(
        root: String,
        filter: String,
    ): ListenableFuture<ListMetadataResponse> {
        val request = ListMetadataRequest.newBuilder()
            .setRoot(root)
            .setFilter(filter)
            .build()

        return futureStub.listMetadata(request)
    }

    /**
     * Publishes a signal value. Used for low frequency signals (e.g. attributes).
     *
     * The server might respond with the following GRPC error codes:
     *     NOT_FOUND if any of the signals are non-existent.
     *     PERMISSION_DENIED
     *         - if access is denied for any of the signals.
     *         - if the signal is already provided by another provider.
     *     INVALID_ARGUMENT
     *        - if the data type used in the request does not match the data type of the addressed signal
     *        - if the published value is not accepted e.g. if sending an unsupported enum value
     */
    fun publishValue(
        signalId: SignalID,
        datapoint: Datapoint,
    ): ListenableFuture<PublishValueResponse> {
        val request = PublishValueRequest.newBuilder()
            .setSignalId(signalId)
            .setDataPoint(datapoint)
            .build()

        return futureStub.publishValue(request)
    }

    /**
     *  Open a stream used to provide actuation and/or publishing values using
     *  a streaming interface. Used to provide actuators and to enable high frequency
     *  updates of values.
     *
     *  The open stream is used for request / response type communication between the
     *  provider and server (where the initiator of a request can vary).
     *  Errors are communicated as messages in the stream.
     */
    fun openProviderStream(
        streamObserver: StreamObserver<OpenProviderStreamResponse>,
    ): StreamObserver<OpenProviderStreamRequest> {
        return asyncStub.openProviderStream(streamObserver)
    }

    /**
     * Gets the server information.
     */
    fun getServerInfo(): ListenableFuture<GetServerInfoResponse> {
        val request = GetServerInfoRequest.newBuilder().build()

        return futureStub.getServerInfo(request)
    }
}
