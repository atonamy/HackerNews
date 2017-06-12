package propertyguru.androidtest.com.hackernews

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import propertyguru.androidtest.com.hackernews.injection.components.DaggerDataManagerComponent
import propertyguru.androidtest.com.hackernews.injection.components.DaggerHackerNewsApiComponent
import propertyguru.androidtest.com.hackernews.injection.components.DataManagerComponent
import propertyguru.androidtest.com.hackernews.injection.components.HackerNewsApiComponent
import propertyguru.androidtest.com.hackernews.injection.modules.DataManagerModule
import propertyguru.androidtest.com.hackernews.injection.modules.HackerNewsApiModule

/**
 * Created by archie on 6/6/17.
 */
class App: Application() {

    companion object {
        lateinit var dataManagerComponent: DataManagerComponent
        lateinit var hackerNewsApiModule: HackerNewsApiComponent
        lateinit var refWatcher: RefWatcher
    }

    override fun onCreate() {
        super.onCreate()
        refWatcher = LeakCanary.install(this)
        dataManagerComponent = DaggerDataManagerComponent.builder().dataManagerModule(DataManagerModule()).build()
        hackerNewsApiModule = DaggerHackerNewsApiComponent.builder().hackerNewsApiModule(HackerNewsApiModule()).build()
    }
}