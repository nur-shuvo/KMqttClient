package com.nurshuvo.kmqtt.internal.flowable

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch

abstract class FlowableWithSingle<T, S> : AbstractFlow<T>() {
    var singleConsumer: ((S) -> Unit)? = null
    private var job: Job? = null

    override suspend fun collectSafely(
        collector: FlowCollector<T>,
    ) {
        coroutineScope {
            val resultChannel = Channel<T>()
            job = launch {
                produceFlow(resultChannel)
            }
            collector.emitAll(resultChannel)
        }
    }

    fun doOnSingle(
        consumer: (S) -> Unit,
    ): FlowableWithSingle<T, S> {
        singleConsumer = consumer
        return this
    }

    protected abstract suspend fun produceFlow(
        resultChannel: Channel<T>,
    )

    suspend fun close() {
        job?.cancelAndJoin()
        singleConsumer = null
    }
}
