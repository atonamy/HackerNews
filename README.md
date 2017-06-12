# HackerNews
Since Google has been announced Kotlin to be one of the first-class languages to be used in Android Development 
I decided to do this test on Kotlin.

Utilizes a simple MVVM pattern, inspired by https://github.com/googlesamples/android-architecture/tree/master. 
Unit tests are provided (including stubs/mocks) 

## Requirements
Minimum API: 16 (can be lower to 11 teoretically, not tested)

## Libraries Used
Dagger2, Coroutines, Anko, Retrofi2, OkHttp3, Realm DB, Gson, Custom Tabs, Leak Canary, Mockito, Awaitility, Espresso

## Features
* Download stories and comments in background (using parallel execution)
* Pull to Refresh
* Multilevel of comments support
* Material design
* Chrome Custom Tabs web view
