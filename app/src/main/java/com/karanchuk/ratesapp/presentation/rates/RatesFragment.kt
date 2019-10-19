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
import com.google.android.material.snackbar.Snackbar
import com.karanchuk.ratesapp.R
import com.karanchuk.ratesapp.data.Currencies
import com.karanchuk.ratesapp.di.Injectable
import com.karanchuk.ratesapp.domain.common.Utils
import com.karanchuk.ratesapp.domain.common.gone
import com.karanchuk.ratesapp.domain.common.livedata.LiveEvent
import com.karanchuk.ratesapp.domain.common.visible
import kotlinx.android.synthetic.main.fragment_rates.*
import javax.inject.Inject

class RatesFragment : Fragment(), Injectable {

    companion object {
        fun newInstance() = RatesFragment()
        private const val SINGLE_LINE_SNACKBAR_HEIGHT_DP = 48
    }

    @Inject lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: RatesViewModel
    private lateinit var ratesListRecycler: RecyclerView
    private lateinit var ratesListAdapter: RatesListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var networkStateSnackbar: Snackbar

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
    }

    override fun onResume() {
        super.onResume()
        viewModel.resumeTimer()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseTimer()
    }

    private fun setupFragment() {
        viewManager = LinearLayoutManager(context)
        ratesListAdapter = RatesListAdapter(
            viewModel::updateCurrentBaseRate
        )

        activity?.let {
            ratesListRecycler = it.findViewById<RecyclerView>(R.id.rates_recycler_view).apply {
                layoutManager = viewManager
                adapter = ratesListAdapter
            }
        }

        networkStateSnackbar = createNetworkStateSnackbar()

        progress.visible()
        rates_recycler_view.gone()
        error.gone()
    }

    private fun bindViewModel() {
        viewModel.rates.observe(this, Observer<List<RateUI>?> { rates ->
            if (rates != null ) {
                ratesListAdapter.updateRates(rates)
                error.gone()
                progress.gone()
                rates_recycler_view.visible()
            } else {
                progress.gone()
                rates_recycler_view.gone()
                error.visible()
            }
        })
        viewModel.networkLiveData.observe(this, Observer<LiveEvent<Boolean>> {
            it.getContentIfNotHandled()?.let { isOnline ->
                if (isOnline) {
                    hideNetworkStateSnackbar()
                    viewModel.resumeTimer()
                } else {
                    showNetworkStateSnackbar()
                    viewModel.pauseTimer()
                }
            }
        })
    }

    private fun createNetworkStateSnackbar(): Snackbar {
        return Snackbar.make(
            requireActivity().findViewById<View>(android.R.id.content),
            R.string.you_are_offline,
            Snackbar.LENGTH_INDEFINITE
        )
    }

    private fun showNetworkStateSnackbar() {
        if (!networkStateSnackbar.isShown) {
            networkStateSnackbar.show()
            ratesListRecycler.setPadding(0, 0, 0,
                Utils.dpToPx(requireContext(), SINGLE_LINE_SNACKBAR_HEIGHT_DP))
        }

    }

    private fun hideNetworkStateSnackbar() {
        if (networkStateSnackbar.isShown) {
            networkStateSnackbar.dismiss()
            ratesListRecycler.setPadding(0, 0, 0, 0)
        }
    }
}
