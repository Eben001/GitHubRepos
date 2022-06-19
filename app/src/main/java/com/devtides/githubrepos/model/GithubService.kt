package com.devtides.githubrepos.model

import com.devtides.githubrepos.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object GithubService {

    private const val BASE_URL = "https://api.github.com/"

    fun getUnauthorizedApi() =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(GithubApi::class.java)


    fun getAuthorizedApi(token:String):GithubApi{
        val okHttpClient = OkHttpClient.Builder()
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        if(BuildConfig.DEBUG){
            okHttpClient.addInterceptor(logging)
        }

        okHttpClient.addInterceptor{chain ->
            val request = chain.request()
            val newRequest = request.newBuilder()
                .addHeader("Authorization", "token ${token}")
                .build()
            chain.proceed(newRequest)
        }

        return  Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(GithubApi::class.java)

    }

}