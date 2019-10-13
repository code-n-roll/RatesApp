package com.karanchuk.ratesapp.ui.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.karanchuk.ratesapp.R
import com.karanchuk.ratesapp.di.Injectable
import javax.inject.Inject

class MainFragment : Fragment(), Injectable {

    companion object {
        fun newInstance() = MainFragment()
    }

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    @Inject lateinit var currencies: Currencies

    private lateinit var viewModel: MainViewModel
    private lateinit var rateListRecycler: RecyclerView
    private lateinit var rateListAdapter: RateListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val currencyValueFocusListener = View.OnFocusChangeListener { view, hasFocus ->
        if (hasFocus) {
            viewModel.pauseTimer()
        } else {
//            hideKeyboard()
            viewModel.resumeTimer()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, vmFactory)[MainViewModel::class.java]

        setupFragment()
        bindViewModel()
        if (savedInstanceState == null) {
            viewModel.resumeTimer()
        }
    }

    private fun setupFragment() {
        viewManager = LinearLayoutManager(context)
        rateListAdapter = RateListAdapter(
            currencyValueFocusListener,
            viewModel::updateCurrentBaseRate,
            viewModel::pauseTimer,
            viewModel::resumeTimer,
            currencies
        )

        activity?.let {
            rateListRecycler = it.findViewById<RecyclerView>(R.id.ratesRecyclerView).apply {
                layoutManager = viewManager
                adapter = rateListAdapter
            }
        }
    }

    private fun bindViewModel() {
        viewModel.rates.observe(this, Observer<List<RateUI>> { rates ->
            rateListAdapter.updateRates(rates)
            rateListAdapter.notifyDataSetChanged()
        })
    }
}
