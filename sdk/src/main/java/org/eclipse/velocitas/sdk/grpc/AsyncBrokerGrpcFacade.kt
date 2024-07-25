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

import io.grpc.Channel
import io.grpc.Context
import io.grpc.stub.StreamObserver
import org.eclipse.kuksa.proto.v2.KuksaValV2.*
import org.eclipse.kuksa.proto.v2.Types.Datapoint
import org.eclipse.kuksa.proto.v2.Types.SignalID
import org.eclipse.kuksa.proto.v2.VALGrpc

/**
 * AsyncBrokerGrpcFacade provides asynchronous communication against the VehicleDataBroker.
 * The current implementation uses the 'kuksa.val.v2' protocol which can be found here: https://github.com/eclipse-kuksa/kuksa-databroker/tree/main/proto/kuksa/val/v1.
 */
class AsyncBrokerGrpcFacade(private val channel: Channel) : GrpcClient() {
    private val asyncStub: VALGrpc.VALStub
        get() = VALGrpc.newStub(channel)

    /**
     * Gets the latest value of a [signalId].
     * If the request is successfully executed the response will be delivered to the [responseHandler],
     * if an error occurs it will be delivered to the [errorHandler].
     *
     * The server might respond with the following GRPC error codes:
     *    NOT_FOUND if the requested signal doesn't exist
     *    PERMISSION_DENIED if access is denied
     */
    fun getValue(
        signalId: SignalID,
        responseHandler: ((GetValueResponse) -> Unit),
        errorHandler: ((Throwable) -> Unit),
    ) {
        val request = GetValueRequest.newBuilder()
            .setSignalId(signalId)
            .build()

        val callData = SingleResponseGrpcCall<GetValueRequest, GetValueResponse>(request)
        addActiveCall(callData)

        val responseObserver = AsyncGrpcObserver<GetValueResponse>()
            .apply {
                onResponseHandler = { response ->
                    responseHandler(response)
                }
                onFinishHandler = {
                    callData.isComplete = true
                }
                onErrorHandler = { throwable ->
                    errorHandler(throwable)
                }
            }

        asyncStub.getValue(request, responseObserver)
    }

    /**
     * Gets the latest values of a set of [signalIds]. The returned list of data points has the same order as the list
     * of the request.
     * If the request is successfully executed the response will be delivered to the [responseHandler],
     * if an error occurs it will be delivered to the [errorHandler].
     *
     * The server might respond with the following GRPC error codes:
     *    NOT_FOUND if any of the requested signals doesn't exist.
     *    PERMISSION_DENIED if access is denied for any of the requested signals.
     */
    fun getValues(
        signalIds: List<SignalID>,
        responseHandler: ((GetValuesResponse) -> Unit),
        errorHandler: ((Throwable) -> Unit),
    ) {
        val request = GetValuesRequest.newBuilder()
            .addAllSignalIds(signalIds)
            .build()

        val callData = SingleResponseGrpcCall<GetValuesRequest, GetValuesResponse>(request)
        addActiveCall(callData)

        val responseObserver = AsyncGrpcObserver<GetValuesResponse>()
            .apply {
                onResponseHandler = { response ->
                    responseHandler(response)
                }
                onFinishHandler = {
                    callData.isComplete = true
                }
                onErrorHandler = { throwable ->
                    errorHandler(throwable)
                }
            }

        asyncStub.getValues(request, responseObserver)
    }

    /**
     * Lists the values of [signalIds] matching the request and responds with a list of signal values. Only values of
     * signals that the user is allowed to read are included (everything else is ignored).
     * If the request is successfully executed the response will be delivered to the [responseHandler], if an error
     * occurs it will be delivered to the [errorHandler].
     *
     * The server might respond with the following GRPC error codes:
     *    NOT_FOUND if any of the requested signals doesn't exist.
     *    PERMISSION_DENIED if access is denied for any of the requested signals.
     */
    fun listValues(
        signalIds: List<SignalID>,
        responseHandler: ((ListValuesResponse) -> Unit),
        errorHandler: ((Throwable) -> Unit),
    ) {
        val request = ListValuesRequest.newBuilder()
            .addAllSignalIds(signalIds)
            .build()

        val callData = StreamingResponseGrpcCall<ListValuesRequest, ListValuesResponse>(request)
        addActiveCall(callData)

        val responseObserver = AsyncGrpcObserver<ListValuesResponse>()
            .apply {
                onResponseHandler = { response ->
                    responseHandler(response)
                }
                onFinishHandler = {
                    callData.isComplete = true
                }
                onErrorHandler = { throwable ->
                    errorHandler(throwable)
                }
            }

        asyncStub.listValues(request, responseObserver)
    }

    /**
     * Subscribes to a set of [signalIds].
     * If the request is successfully executed the responses will be delivered to the [responseHandler], if an error
     * occurs it will be delivered to the [errorHandler].
     * Returns a cancellableContext to cancel the active subscriptions.
     *
     * The server might respond with the following GRPC error codes:
     *    NOT_FOUND if any of the signals are non-existent.
     *    PERMISSION_DENIED if access is denied for any of the signals.
     */
    fun subscribe(
        signalIds: List<SignalID>,
        responseHandler: ((SubscribeResponse) -> Unit),
        errorHandler: ((Throwable) -> Unit),
    ): Context.CancellableContext {
        val request = SubscribeRequest.newBuilder()
            .addAllSignalIds(signalIds)
            .build()

        val callData = StreamingResponseGrpcCall<SubscribeRequest, SubscribeResponse>(request)
        addActiveCall(callData)

        val responseObserver = AsyncGrpcObserver<SubscribeResponse>()
            .apply {
                onResponseHandler = { response ->
                    callData.lastResponse = response
                    responseHandler(response)
                }
                onFinishHandler = {
                    callData.isComplete = true
                }
                onErrorHandler = { throwable ->
                    errorHandler(throwable)
                }
            }

        val currentContext = Context.current()
        val cancellableContext = currentContext.withCancellation()
        cancellableContext.run {
            asyncStub.subscribe(callData.request, responseObserver)
        }
        return cancellableContext
    }

    /**
     * Actuates a single actuator with the specified [signalId].
     * If the request is successfully executed the response will be delivered to the [responseHandler], if an error
     * occurs it will be delivered to the [errorHandler].
     *
     * The server might respond with the following GRPC error codes:
     *    NOT_FOUND if the actuator does not exist.
     *    PERMISSION_DENIED if access is denied for of the actuator.
     *    UNAVAILABLE if there is no provider currently providing the actuator
     *    INVALID_ARGUMENT
     *        - if the data type used in the request does not match the data type of the addressed signal
     *        - if the requested value is not accepted, e.g. if sending an unsupported enum value
     */
    fun actuate(
        signalId: SignalID,
        responseHandler: ((ActuateResponse) -> Unit),
        errorHandler: ((Throwable) -> Unit),
    ) {
        val request = ActuateRequest.newBuilder()
            .setSignalId(signalId)
            .build()

        val callData = SingleResponseGrpcCall<ActuateRequest, ActuateResponse>(request)
        val responseObserver = AsyncGrpcObserver<ActuateResponse>()
            .apply {
                onResponseHandler = { response ->
                    responseHandler(response)
                }
                onFinishHandler = {
                    callData.isComplete = true
                }
                onErrorHandler = { throwable ->
                    errorHandler(throwable)
                }
            }

        asyncStub.actuate(request, responseObserver)
    }

    /**
     * Actuates simultaneously multiple actuators with the specified [signalIds].
     * If any error occurs, the entire operation will be aborted and no single actuator value will be forwarded to the
     * provider.
     * If the request is successfully executed the response will be delivered to the [responseHandler], if an error
     * occurs it will be delivered to the [errorHandler].
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
    fun batchActuate(
        signalIds: List<SignalID>,
        responseHandler: ((BatchActuateResponse) -> Unit),
        errorHandler: ((Throwable) -> Unit),
    ) {
        val requestBuilder = BatchActuateRequest.newBuilder()

        signalIds.forEach { signalId ->
            val actuateRequest = ActuateRequest.newBuilder().setSignalId(signalId)
            requestBuilder.addActuateRequests(actuateRequest)
        }

        val request = requestBuilder.build()

        val callData = SingleResponseGrpcCall<BatchActuateRequest, BatchActuateResponse>(request)
        addActiveCall(callData)

        val responseObserver = AsyncGrpcObserver<BatchActuateResponse>()
            .apply {
                onResponseHandler = { response ->
                    responseHandler(response)
                }
                onFinishHandler = {
                    callData.isComplete = true
                }
                onErrorHandler = { throwable ->
                    errorHandler(throwable)
                }
            }

        asyncStub.batchActuate(request, responseObserver)
    }

    /**
     * Lists metadata of signals matching the request.
     * If any error occurs, the entire operation will be aborted and no single actuator value will be forwarded to the
     * provider.
     * If the request is successfully executed the response will be delivered to the [responseHandler], if an error
     * occurs it will be delivered to the [errorHandler].
     *
     * The server might respond with the following GRPC error codes:
     *     NOT_FOUND if the specified root branch does not exist.
     */
    fun listMetadata(
        root: String,
        filter: String,
        responseHandler: ((ListMetadataResponse) -> Unit),
        errorHandler: ((Throwable) -> Unit),
    ) {
        val request = ListMetadataRequest.newBuilder()
            .setRoot(root)
            .setFilter(filter)
            .build()

        val callData = SingleResponseGrpcCall<ListMetadataRequest, ListMetadataResponse>(request)
        addActiveCall(callData)

        val responseObserver = AsyncGrpcObserver<ListMetadataResponse>()
            .apply {
                onResponseHandler = { response ->
                    responseHandler(response)
                }
                onFinishHandler = {
                    callData.isComplete = true
                }
                onErrorHandler = { throwable ->
                    errorHandler(throwable)
                }
            }

        asyncStub.listMetadata(request, responseObserver)
    }

    /**
     * Publishes a signal value. Used for low frequency signals (e.g. attributes).
     * If the request is successfully executed the response will be delivered to the [responseHandler], if an error
     * occurs it will be delivered to the [errorHandler].
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
        responseHandler: ((PublishValueResponse) -> Unit),
        errorHandler: ((Throwable) -> Unit),
    ) {
        val request = PublishValueRequest.newBuilder()
            .setSignalId(signalId)
            .setDataPoint(datapoint)
            .build()

        val callData = SingleResponseGrpcCall<PublishValueRequest, PublishValueResponse>(request)
        addActiveCall(callData)

        val responseObserver = AsyncGrpcObserver<PublishValueResponse>()
            .apply {
                onResponseHandler = { response ->
                    responseHandler(response)
                }
                onFinishHandler = {
                    callData.isComplete = true
                }
                onErrorHandler = { throwable ->
                    errorHandler(throwable)
                }
            }

        asyncStub.publishValue(request, responseObserver)
    }

    /**
     *  Open a stream used to provide actuation and/or publishing values using
     *  a streaming interface. Used to provide actuators and to enable high frequency
     *  updates of values.
     *
     *  The open stream is used for request / response type communication between the
     *  provider and server (where the initiator of a request can vary).
     *  Errors are communicated as messages in the stream.
     *
     * If the request is successfully executed the responses will be delivered to the [responseHandler], if an error
     * occurs it will be delivered to the [errorHandler].
     */
    fun openProviderStream(
        responseHandler: ((OpenProviderStreamResponse) -> Unit),
        errorHandler: ((Throwable) -> Unit),
    ): StreamObserver<OpenProviderStreamRequest> {
        val callData = BidiStreamingResponseGrpcCall<OpenProviderStreamRequest, OpenProviderStreamResponse>()
        addActiveCall(callData)

        val responseObserver = AsyncGrpcObserver<OpenProviderStreamResponse>()
            .apply {
                onResponseHandler = { response ->
                    callData.lastResponse = response
                    responseHandler(response)
                }
                onFinishHandler = {
                    callData.isComplete = true
                }
                onErrorHandler = { throwable ->
                    errorHandler(throwable)
                }
            }

        return asyncStub.openProviderStream(responseObserver)
    }

    /**
     * Gets the server information.
     * If the request is successfully executed the responses will be delivered to the [responseHandler], if an error
     * occurs it will be delivered to the [errorHandler].
     */
    fun getServerInfo(
        responseHandler: ((GetServerInfoResponse) -> Unit),
        errorHandler: ((Throwable) -> Unit),
    ) {
        val request = GetServerInfoRequest.newBuilder().build()

        val callData = SingleResponseGrpcCall<GetServerInfoRequest, GetServerInfoResponse>(request)
        addActiveCall(callData)

        val responseObserver = AsyncGrpcObserver<GetServerInfoResponse>()
            .apply {
                onResponseHandler = { response ->
                    responseHandler(response)
                }
                onFinishHandler = {
                    callData.isComplete = true
                }
                onErrorHandler = { throwable ->
                    errorHandler(throwable)
                }
            }

        asyncStub.getServerInfo(request, responseObserver)
    }
}
