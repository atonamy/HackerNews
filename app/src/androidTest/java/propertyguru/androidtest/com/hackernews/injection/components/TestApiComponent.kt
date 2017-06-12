package propertyguru.androidtest.com.hackernews.injection.components

import dagger.Component
import propertyguru.androidtest.com.hackernews.injection.modules.DataManagerModule
import propertyguru.androidtest.com.hackernews.main.CommentsDataTest
import propertyguru.androidtest.com.hackernews.main.HomeScreenActivityTest
import propertyguru.androidtest.com.hackernews.main.ItemScreenActivityTest
import propertyguru.androidtest.com.hackernews.main.StoriesDataTest
import javax.inject.Singleton

/**
 * Created by archie on 11/6/17.
 */
@Singleton
@Component(modules = arrayOf(DataManagerModule::class))
interface TestApiComponent: DataManagerComponent {
    fun inject(obj: StoriesDataTest)
    fun inject(obj: CommentsDataTest)
    fun inject(obj: HomeScreenActivityTest)
    fun inject(obj: ItemScreenActivityTest)
}