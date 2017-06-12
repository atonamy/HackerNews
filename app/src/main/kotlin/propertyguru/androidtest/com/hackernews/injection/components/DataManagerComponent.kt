package propertyguru.androidtest.com.hackernews.injection.components

import dagger.Component
import propertyguru.androidtest.com.hackernews.data.DataManager
import propertyguru.androidtest.com.hackernews.injection.modules.DataManagerModule
import javax.inject.Singleton

/**
 * Created by archie on 6/6/17.
 */

@Singleton
@Component(modules = arrayOf(DataManagerModule::class))
interface DataManagerComponent {
    fun inject(obj: DataManager)
}