package velocitas.sdk.middleware

import velocitas.SimpleUrlParse
import velocitas.getEnvVar
import velocitas.sdk.Logger

class NativeMiddleware : Middleware(TYPE_ID) {

    override fun getServiceLocation(serviceName: String): String {
        val envVarName = getServiceEnvVarName(serviceName)
        val envVar = getEnvVar(envVarName)
        val serviceAddress: String = SimpleUrlParse(envVar).netLocation
        if (serviceAddress.isNotEmpty()) {
            return serviceAddress
        }

        val defaultServiceAddress = getDefaultLocation(serviceName)
        if (defaultServiceAddress?.isNotEmpty() == true) {
            Logger.warn("Env variable '$envVarName' defining location of " +
                    "service '$serviceName' not properly set. Taking default: '$serviceAddress'")
            return defaultServiceAddress
        }

        val errorMessage = "Env variable '$envVarName' defining location of " +
                "service '$serviceName' not set. Please define!"
        Logger.error(errorMessage)
        throw RuntimeException(errorMessage)
    }

    companion object {
        const val TYPE_ID = "native"

        val DEFAULT_LOCATIONS = mapOf(
            "mqtt" to "localhost:1883",
            "vehicledatabroker" to "localhost:55555"
        )

        fun getDefaultLocation(serviceName: String): String? {
            val filteredService =
                DEFAULT_LOCATIONS.entries.find { it.key == serviceName.lowercase() }
            if (filteredService != null) {
                val parse = SimpleUrlParse(filteredService.value)
                return parse.netLocation
            }
            return null
        }

        fun getServiceEnvVarName(serviceName: String): String {
            return "SDV_${serviceName.uppercase()}_ADDRESS"
        }
    }
}
