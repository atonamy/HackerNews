package propertyguru.androidtest.com.hackernews.utils

import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import okio.BufferedSource
import okhttp3.ResponseBody
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import okhttp3.MediaType
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




/**
 * Created by archie on 11/6/17.
 */
class MockCall<T>(val response: T, val responseCode: Int = 200): Call<T> {
    override fun isExecuted(): Boolean {
        return false
    }

    override fun cancel() {

    }

    override fun clone(): Call<T> {
        return MockCall(response)
    }

    override fun request(): Request? {
       return null
    }

    override fun enqueue(callback: Callback<T>?) {
        if (responseCode > 0) {
            callback?.onResponse(null, buildResponse())
        } else {
            callback?.onFailure(null, null)
        }
    }

    override fun isCanceled(): Boolean {
        return false
    }

    override fun execute(): Response<T> {
        return buildResponse()
    }

    private fun buildResponse(): Response<T> {
        if (responseCode in 200..299) {
            return Response.success(response)
        } else {
            return Response.error(responseCode, DummyResponseBody())
        }
    }

    private class DummyResponseBody : ResponseBody() {
        override fun contentType(): MediaType? {
            return null
        }

        override fun contentLength(): Long {
            return 0
        }

        override fun source(): BufferedSource? {
            return null
        }
    }

}