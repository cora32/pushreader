package io.alexarix.pushreader

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.alexarix.pushreader.repo.RestApi
import io.alexarix.pushreader.repo.getRetrofit
import io.alexarix.pushreader.repo.room.PRDao
import io.alexarix.pushreader.repo.room.getDB
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.java

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention
@Qualifier
annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
object HiltModules {
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineContext = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineContext = Dispatchers.Default

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineContext = Dispatchers.Main

    @Provides
    @Singleton
    fun getRestApi(@ApplicationContext context: Context): RestApi =
        getRetrofit(context)
            .create(RestApi::class.java)

    @Provides
    @Singleton
    fun getDao(@ApplicationContext context: Context): PRDao = getDB(context).dao()

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context) = context.applicationContext
}
