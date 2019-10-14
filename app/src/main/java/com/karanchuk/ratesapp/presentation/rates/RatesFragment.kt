package com.karanchuk.ratesapp.presentation.rates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.karanchuk.ratesapp.R
import com.karanchuk.ratesapp.data.Currencies
import com.karanchuk.ratesapp.di.Injectable
import javax.inject.Inject

class RatesFragment : Fragment(), Injectable {

    companion object {
        fun newInstance() = RatesFragment()
    }

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    @Inject lateinit var currencies: Currencies

    private lateinit var viewModel: RatesViewModel
    private lateinit var ratesListRecycler: RecyclerView
    private lateinit var ratesListAdapter: RatesListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_rates, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, vmFactory)[RatesViewModel::class.java]

        setupFragment()
        bindViewModel()
        if (savedInstanceState == null) {
            viewModel.resumeTimer()
        }
    }

    private fun setupFragment() {
        viewManager = LinearLayoutManager(context)
        ratesListAdapter = RatesListAdapter(
            viewModel::updateCurrentBaseRate,
            viewModel::pauseTimer,
            viewModel::resumeTimer,
            currencies
        )

        activity?.let {
            ratesListRecycler = it.findViewById<RecyclerView>(R.id.ratesRecyclerView).apply {
                layoutManager = viewManager
                adapter = ratesListAdapter
            }
        }
    }

    private fun bindViewModel() {
        viewModel.rates.observe(this, Observer<List<RateUI>> { rates ->
            ratesListAdapter.updateRates(rates)
        })
    }
}
