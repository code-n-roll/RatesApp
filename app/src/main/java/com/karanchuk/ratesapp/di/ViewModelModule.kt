package com.karanchuk.ratesapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.karanchuk.ratesapp.presentation.rates.RatesViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(RatesViewModel::class)
    abstract fun bindRatesViewModel(viewModel: RatesViewModel): ViewModel
}