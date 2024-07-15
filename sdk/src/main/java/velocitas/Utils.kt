package velocitas

import java.net.URI
import java.net.URL

fun getEnvVar(varName: String, defaultValue: String = ""): String {
    val envVar = System.getenv(varName)
    if (envVar != null) {
        return envVar
    }
    return defaultValue
}

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
class SimpleUrlParse(url: String) {
    private val SCHEME_PART_START = "//"
    private val SIMPLIFIED_SCHEME_SEPARATOR = "://"

    /**
     * The parsed scheme which can be the empty string if the URL does not contain a scheme
     */
    var scheme: String = ""
        private set

    /**
     * The network location part of the parsed URL
     *
     * This is the part between the leading double slashes and the first slash after,
     * e.g. URL = "http://username:password@hostname:portnumber/path"
     * --> netLocation = "username:password@hostname:portnumber"
     */
    var netLocation: String = ""
        private set

    init {
        parse(url)
    }

    private fun parse(url: String) {
        var schemeLen = url.indexOf(SIMPLIFIED_SCHEME_SEPARATOR)
        if (schemeLen > -1) {
            this.scheme = url.substring(0, schemeLen).lowercase()
        } else {
            schemeLen = 0
        }

        var startOfSchemePart = url.indexOf(SCHEME_PART_START, schemeLen)
        if (startOfSchemePart > -1) {
            startOfSchemePart += SCHEME_PART_START.length
        } else {
            startOfSchemePart = 0
        }

        var netLocationLen = url.indexOf("/", startOfSchemePart)
        if (netLocationLen == -1) {
            netLocationLen = url.length
        }
        this.netLocation = url.substring(startOfSchemePart, netLocationLen)
    }
}