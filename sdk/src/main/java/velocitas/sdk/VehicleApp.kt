package velocitas.sdk

import velocitas.sdk.middleware.Middleware

/**
 * Base class for all vehicle apps which manages an app's lifecycle.
 */
abstract class VehicleApp
{

    /**
     * Runs the Vehicle App.
     */
    fun run() {
        Logger.info("Running App...")
        Middleware.getInstance().start()
        Middleware.getInstance().waitUntilReady()

        onStart()

        // TODO: Fix busy waiting
        while (true) {
            Thread.sleep(1)
        }
    }

    /**
     * Stops the Vehicle App
     */
    fun stop() {
        Logger.info("Stopping App...")

        onStop()

        Middleware.getInstance().stop()
    }

    /**
     * Event which is called once the Vehicle App is started.
     */
    abstract fun onStart()

    /**
     * Event which is called once the Vehicle App is requested to stop.
     */
    abstract fun onStop()
}
