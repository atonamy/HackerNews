package propertyguru.androidtest.com.hackernews.injection.components

import dagger.Component
import propertyguru.androidtest.com.hackernews.injection.modules.DataManagerModuleTest
import propertyguru.androidtest.com.hackernews.injection.modules.HackerNewsApiModule
import javax.inject.Singleton

/**
 * Created by archie on 11/6/17.
 */
@Singleton
@Component(modules = arrayOf(HackerNewsApiModule::class))
interface DataManagerModuleTestComponent: HackerNewsApiComponent {
    fun inject(obj: DataManagerModuleTest)
}