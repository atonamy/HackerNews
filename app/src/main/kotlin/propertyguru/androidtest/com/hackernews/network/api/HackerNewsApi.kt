package propertyguru.androidtest.com.hackernews.network.api

import propertyguru.androidtest.com.hackernews.data.model.Comment
import propertyguru.androidtest.com.hackernews.data.model.Story
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by archie on 6/6/17.
 */
interface HackerNewsApi {

    /**
     * Return a list of the latest post IDs.
     */
    @get:GET("topstories.json")
    val topStories: Call<List<Long>>


    /**
     * Return story item.
     */
    @GET("item/{itemId}.json")
    fun getStoryItem(@Path("itemId") itemId: Long): Call<Story>

    /**
     * Returns comment item.
     */
    @GET("item/{itemId}.json")
    fun getCommentItem(@Path("itemId") itemId: Long): Call<Comment>

}