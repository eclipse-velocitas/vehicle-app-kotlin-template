package velocitas.sdk.grpc

import velocitas.sdk.RecurringJob
import velocitas.sdk.ThreadPool

class GrpcClient {
    private val recurringJob: RecurringJob
    private val activeCalls = mutableListOf<GrpcCall>()

    val numActiveCalls: Int
        get() {
            return activeCalls.size
        }

    init {
        recurringJob = RecurringJob {
            pruneCompletedRequests()
        }
        ThreadPool.getInstance().enqueue(recurringJob)
    }

   @Synchronized
    fun addActiveCall(call: GrpcCall) {
        activeCalls.add(call)
    }

    @Synchronized
    private fun pruneCompletedRequests() {
        val iterator = activeCalls.iterator()
        iterator.forEach { grpcCall ->
            if (grpcCall.isComplete) {
                iterator.remove()
            }
        }

        // static auto isComplete = [](const auto& activeCall) { return activeCall->m_isComplete; };

        // {
        //    std::scoped_lock<std::mutex> lock(m_mutex);
        //    m_activeCalls.erase(std::remove_if(m_activeCalls.begin(), m_activeCalls.end(), isComplete),
        //        m_activeCalls.end());
        // }
    }
}
