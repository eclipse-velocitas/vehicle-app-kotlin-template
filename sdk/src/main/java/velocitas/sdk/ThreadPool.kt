package velocitas.sdk

import java.util.Vector
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Manages a pool of threads which are capable of executing jobs asynchronously.
 */
class ThreadPool(var numWorkerThreads: Int) {
    constructor() : this(2)

    private val workerThreads = Vector<Thread>()
    private val jobs = ArrayBlockingQueue<IJob>(1000)
    private val isRunning: AtomicBoolean = AtomicBoolean(true)

    init {
        repeat(numWorkerThreads) {
            val thread = thread {
                threadLoop()
            }

            workerThreads.add(thread)
        }
    }

    fun enqueue(job: IJob) {
        jobs.add(job)
    }

    fun shutdown() {
        isRunning.set(false)
    }

    private fun threadLoop() {
        while (isRunning.get()) {
            val polledJob = jobs.poll()
            if (polledJob != null) {
                try {
                    polledJob.execute()
                    if (polledJob.shallRecur()) {
                        enqueue(polledJob)
                    }
                } catch (e: Exception) {
                    Logger.error("Uncaught Exception in job execution: ${e.message}")
                }
            }
        }
    }

    companion object : NoArgumentSingletonHolder<ThreadPool>(::ThreadPool)
}
