package propertyguru.androidtest.com.hackernews.injection.components

import dagger.Component
import propertyguru.androidtest.com.hackernews.injection.modules.HackerNewsApiModule
import propertyguru.androidtest.com.hackernews.network.api.HackerNewsApiHelper
import javax.inject.Singleton

/**
 * Created by archie on 6/6/17.
 */

@Singleton
@Component(modules = arrayOf(HackerNewsApiModule::class))
interface HackerNewsApiComponent {
    fun inject(obj: HackerNewsApiHelper)
}