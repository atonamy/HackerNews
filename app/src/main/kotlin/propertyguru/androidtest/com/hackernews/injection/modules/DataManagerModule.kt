package propertyguru.androidtest.com.hackernews.injection.modules

import dagger.Module
import dagger.Provides
import propertyguru.androidtest.com.hackernews.network.api.HackerNewsApi
import propertyguru.androidtest.com.hackernews.network.api.HackerNewsApiHelper
import javax.inject.Singleton

/**
 * Created by archie on 6/6/17.
 */

@Module
open class DataManagerModule {

    @Singleton
    @Provides
    open fun provideHackerNewsApi(): HackerNewsApi {
        return HackerNewsApiHelper().create()
    }
}