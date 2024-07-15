package velocitas.sdk

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
