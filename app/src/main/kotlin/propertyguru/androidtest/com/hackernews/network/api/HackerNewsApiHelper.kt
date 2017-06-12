package propertyguru.androidtest.com.hackernews.network.api

import propertyguru.androidtest.com.hackernews.App

/**
 * Created by archie on 6/6/17.
 */
class HackerNewsApiHelper: RetrofitApiHelper<HackerNewsApi>() {

    val endPoint = "https://hacker-news.firebaseio.com/v0/"

    init {
        App.hackerNewsApiModule.inject(this)
    }

    override fun create(): HackerNewsApi {
        return createRetrofit(endPoint).create(HackerNewsApi::class.java)
    }
}