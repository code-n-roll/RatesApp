package com.karanchuk.ratesapp.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.karanchuk.ratesapp.R
import com.karanchuk.ratesapp.di.Injectable
import javax.inject.Inject

class MainFragment : Fragment(), Injectable {

    companion object {
        fun newInstance() = MainFragment()
    }

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: MainViewModel
    private lateinit var rateListRecycler: RecyclerView
    private lateinit var rateListAdapter: RateListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, vmFactory)[MainViewModel::class.java]

        viewManager = LinearLayoutManager(context)
        rateListAdapter = RateListAdapter(emptyList())

        activity?.let {
            rateListRecycler = it.findViewById<RecyclerView>(R.id.ratesRecyclerView).apply {
                layoutManager = viewManager
                adapter = rateListAdapter
            }
        }

        viewModel.rates.observe(this, Observer<List<RateUI>> { rates ->
            rateListAdapter.setRates(rates)
            rateListAdapter.notifyDataSetChanged()
        })
    }
}
