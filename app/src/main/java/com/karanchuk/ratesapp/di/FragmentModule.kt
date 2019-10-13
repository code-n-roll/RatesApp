package com.karanchuk.ratesapp.di

import com.karanchuk.ratesapp.presentation.rates.RatesFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeRatesFragment(): RatesFragment
}