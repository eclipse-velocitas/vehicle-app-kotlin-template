package velocitas.sdk

/**
 * Status of an asynchronous request.
 *
 * @constructor Construct a new Status object without any errors.
 */
class Status() {
    /**
     * Returns whether the reported status is OK or not.
     *
     * @return true   Status is OK
     * @return false  Status is not OK
     */
    var isOk = true
        private set

    /**
     * Returns whether the reported status is OK or not.
     *
     * @return true   Status is OK
     * @return false  Status is not OK
     */
    var errorMessage: String? = null
        private set

    /**
     * Construct a new error status.
     *
     * @param errorMsg
     */
    constructor(errorMsg: String) : this() {
        this.isOk = false
        this.errorMessage = errorMsg
    }
}
