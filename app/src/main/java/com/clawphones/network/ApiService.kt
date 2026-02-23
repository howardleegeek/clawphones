package com.clawphones.network

import java.io.IOException
import java.util.logging.Logger
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val retryDelayMillis: Long = 1_000L,
    private val sleeper: (Long) -> Unit = { Thread.sleep(it) },
    private val logger: Logger = Logger.getLogger(RetryInterceptor::class.java.name)
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var lastException: IOException? = null
        while (attempt <= maxRetries) {
            try {
                val response = chain.proceed(chain.request())
                // Success -> return immediately
                if (response.isSuccessful) return response

                // For server errors (5xx), retry if we have retries left
                val code = response.code
                if (code in 500..599 && attempt < maxRetries) {
                    response.close()
                    logRetry(attempt + 1)
                    sleeper(retryDelayMillis)
                    attempt += 1
                    continue
                }
                // For client errors (4xx) or exhausted retries, return the response
                return response
            } catch (exception: IOException) {
                if (attempt == maxRetries) throw exception
                lastException = exception
            }
            // Delay before next retry on IOException or server error retry decision
            logRetry(attempt + 1)
            sleeper(retryDelayMillis)
            attempt += 1
        }
        throw lastException ?: IOException("Request failed [task_id=G10-02-CP]")
    }

    private fun logRetry(nextAttempt: Int) {
        logger.warning(
            "Retrying network request [task_id=G10-02-CP, attempt=$nextAttempt, delay_ms=$retryDelayMillis]"
        )
    }
}

object ApiService {
    fun createHttpClient(interceptor: Interceptor = RetryInterceptor()): OkHttpClient {
        return OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .addInterceptor(interceptor)
            .build()
    }

    fun createRetrofit(baseUrl: String, client: OkHttpClient = createHttpClient()): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
