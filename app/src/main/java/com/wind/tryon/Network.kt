package com.wind.tryon

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface RequestService {

    @GET("hello")
    suspend fun hello(): Hello

    @Headers("Content-type:application/json;charset=UTF-8")
    @POST("api/send/file")
    suspend fun tryOn(@Body route: RequestBody): TryOnResult

}

@Immutable
data class TryOnResult(
    val message: String,
    val image_url: String,
)
@Immutable
data class Hello(
    val message: String,
)

class Network {
    companion object {
        //创建拦截器
        private val interceptor = Interceptor { chain ->
            val request = chain.request()
            val requestBuilder = request.newBuilder()
            val url = request.url()
            val builder = url.newBuilder()
            requestBuilder.url(builder.build())
                .method(request.method(), request.body())
                .addHeader("clientType", "Android")
                .addHeader("Content-Type", "application/json")
            chain.proceed(requestBuilder.build())
        }

        //创建OKhttp
        private val client: OkHttpClient.Builder = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)

        //创建retrofit
        private var retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(client.build())
            .build()

        var service: RequestService = retrofit.create(RequestService::class.java)
    }
}

class ImageRepository{
    companion object {

        fun getHello(): Flow<Hello> = flow {
            val hello = Network.service.hello()
            emit(hello)
        }.flowOn(Dispatchers.IO)

        fun getImage(body: RequestBody): Flow<TryOnResult> = flow {
            val tryOn = Network.service.tryOn(body)
            emit(tryOn)
        }.flowOn(Dispatchers.IO)
    }
}

class HelloViewModel : ViewModel() {
    private val helloData: MutableState<Hello?> = mutableStateOf(null)

    fun getHelloData() {
        viewModelScope.launch {
            ImageRepository.getHello()
                .onStart {
                    helloData.value = null
                    Log.d("test", "start")
                }
                .catch { e ->
                    run {
                        e.printStackTrace()
                        helloData.value = Hello(message = "error")
                        Log.d("test", "error")
                    }
                }
                .collect {
                    helloData.value = it
                    Log.d("test", "collect")
                }
        }
    }
}

class ImageViewModel: ViewModel() {
    val imageData: MutableState<TryOnResult?> = mutableStateOf(null)
    val loadState: MutableState<Int> = mutableStateOf(1)
    fun getImageData(clothBase64String: String, personBase64String: String) {
        viewModelScope.launch {
            var gson = Gson()
            var map = HashMap<String, String>()
            map["cloth"] = clothBase64String
            map["person"] = personBase64String
            val strEntity = gson.toJson(map)
            val body = RequestBody.create(
                okhttp3.MediaType.parse("application/json;charset=UTF-8"),
                strEntity
            )
            ImageRepository.getImage(body)
                .onStart {
                    imageData.value = null
                    loadState.value = 0
                    Log.d("test", "start")
                }
                .catch { e ->
                    run {
                        e.printStackTrace()
                        imageData.value = TryOnResult(message = "error", image_url = "null")
                        loadState.value = -1
                        Log.d("test", "error")
                    }
                }
                .collect {
                    imageData.value = it
                    loadState.value = 1
                    Log.d("test", "collect")
                }
        }
    }
}