package com.pepdeal.infotech.di

import org.koin.core.context.startKoin
import org.koin.core.logger.PrintLogger
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        logger(PrintLogger())
        modules(sharedModule, platformModule)
    }
}