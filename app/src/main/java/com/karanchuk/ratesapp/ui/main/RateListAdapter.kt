package com.karanchuk.ratesapp.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.karanchuk.ratesapp.R
import kotlinx.android.synthetic.main.item_rate.view.*

class RateListAdapter(
    private var rates: List<RateUI>
) : RecyclerView.Adapter<RateListAdapter.RateViewHolder>() {

    fun setRates(rates: List<RateUI>) {
        this.rates = rates
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RateViewHolder {
        val item = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rate, parent, false)
        return RateViewHolder(item)
    }

    override fun getItemCount() = rates.size

    override fun onBindViewHolder(holder: RateViewHolder, position: Int) {
        holder.bind(rates[position])
    }

    class RateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(rateUI: RateUI) {
            itemView.inputCurrencyAmount.setText(rateUI.amount)
        }
    }
}

