package io.pleo.antaeus.core.utils

import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import java.io.IOException
import java.time.Duration
import java.util.concurrent.Callable

class RetryUtils(name: String, maxAttempts: Int = 3, vararg retryExceptions: Class<out Throwable>) {
    private var config: RetryConfig = RetryConfig.custom<Any>()
            .maxAttempts(maxAttempts)
            .waitDuration(Duration.ofMillis(1000))
            .failAfterMaxAttempts(true)
            .retryExceptions(*retryExceptions, IOException::class.java)
            .build()

    private val retry: Retry = RetryRegistry.of(config).retry(name)

    fun <T> retry(executable: () -> T): T {
        return retry.executeCallable (executable)
    }
}
