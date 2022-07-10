package com.philkes.pin2pdf.api

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class TaskRunner {
    private val executor: Executor =
        Executors.newFixedThreadPool(3)
    private val handler: Handler = Handler(Looper.getMainLooper())


    fun <R> executeAsync(callable: Callable<R>, onComplete: (R) -> Unit) {
        executor.execute {
            val result: R = callable.call()
            handler.post { onComplete(result) }
        }
    }
}