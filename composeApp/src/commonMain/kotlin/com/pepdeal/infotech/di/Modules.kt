package com.pepdeal.infotech.di

import com.pepdeal.infotech.core.data.HttpClientFactory
import com.pepdeal.infotech.dataStore.PreferencesRepository
import com.pepdeal.infotech.dataStore.PreferencesRepositoryImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


expect val platformModule: Module

val sharedModule = module {
    single { HttpClientFactory.create(get()) }
    singleOf(::PreferencesRepositoryImpl).bind<PreferencesRepository>()
}
