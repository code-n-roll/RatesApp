package com.karanchuk.ratesapp.ui.main

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.karanchuk.ratesapp.R
import com.karanchuk.ratesapp.common.Utils
import kotlinx.android.synthetic.main.item_rate.view.*
import java.util.*
import kotlin.collections.ArrayList

class RateListAdapter(
    private val currencyValueFocusListener: View.OnFocusChangeListener,
    private val setCurrentBaseRate: (RateUI) -> Unit,
    private val pauseTimer: () -> Unit,
    private val resumeTimer: () -> Unit,
    private val currencies: Currencies
) : RecyclerView.Adapter<RateListAdapter.RateViewHolder>() {

    private val rates: ArrayList<RateUI> = ArrayList()
    private val savedRates = ArrayList<RateUI>()

    fun updateRates(rates: List<RateUI>) {
        this.rates.clear()
        this.rates.addAll(rates)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RateViewHolder {
        val item = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rate, parent, false)
        return RateViewHolder(item)
    }

    override fun getItemCount() = rates.size

    override fun onBindViewHolder(holder: RateViewHolder, position: Int) {
        holder.bind(
            rates[position],
            currencyValueFocusListener,
            setCurrentBaseRate,
            pauseTimer,
            resumeTimer
        )
    }

    inner class RateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val currencyFlag: ImageView = itemView.currency_flag
        private val currencyValue: ClearFocusEditText = itemView.currency_value
        private val currencyCode: TextView = itemView.currency_code
        private val currencyName: TextView = itemView.currency_name
        private val currencyValueEditorAction = TextView.OnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                val imm = itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(itemView.windowToken, 0)
                currencyValue.clearFocus()
                return@OnEditorActionListener false
            }
            return@OnEditorActionListener false
        }

        private val textWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (layoutPosition == 0 && itemView.hasFocus()) {
                    val baseRateAmount = if (p0.toString().isEmpty() || p0.toString() == "0") {
                        ""
                    } else {
                        p0.toString()
                    }

                    if (rates[0].amount.isNotEmpty() && baseRateAmount.isEmpty()) {
                        savedRates.clear()
                        savedRates.addAll(rates)

                        rates.clear()
                        savedRates.forEach {
                            rates.add(RateUI("", it.currencyCode, it.currencyName))
                        }
                        notifyDataSetChanged()
                        return
                    } else if (rates[0].amount.isEmpty() && baseRateAmount.isNotEmpty()) {
                        rates.clear()
                        rates.addAll(savedRates)
                    } else if (rates[0].amount.isEmpty() && baseRateAmount.isEmpty()) {
                        return
                    }

                    for ((i, rate) in rates.withIndex()) {
                        if (i != 0) {
                            val rateAmountPerOne = rate.amount.toDouble() / rates[0].amount.toDouble()
                            val count = baseRateAmount.toDouble()
                            rates[i].amount = (count * rateAmountPerOne).toString()
                        }
                    }
                    updateCurrentBaseRate(baseRateAmount)

                    Utils.roundRateAmounts(rates)
                    notifyDataSetChanged()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // nop
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // nop
            }
        }
        lateinit var setCurrentBaseRate: (RateUI) -> Unit
        lateinit var pauseTimer: () -> Unit
        lateinit var resumeTimer: () -> Unit

        private fun swapSelectedItemAndFirst() {
            val ratesLinkedList = LinkedList(rates)

            layoutPosition.takeIf { it > 0 }?.also { currentPosition ->
                ratesLinkedList.removeAt(currentPosition).also {
                    ratesLinkedList.addFirst(it)
                }
                rates.clear()
                rates.addAll(ratesLinkedList)
                notifyItemMoved(currentPosition, 0)
            }
        }

        private fun updateCurrentBaseRate(baseRate: RateUI) {
            setCurrentBaseRate(baseRate)
        }

        private fun updateCurrentBaseRate(baseAmount: String) {
            val baseRate = rates[0]
            baseRate.amount = baseAmount
            setCurrentBaseRate(baseRate)
        }

        fun bind(
            rateUI: RateUI,
            currencyValueFocusListener: View.OnFocusChangeListener,
            setCurrentBaseRate: (RateUI) -> Unit,
            pauseTimer: () -> Unit,
            resumeTimer: () -> Unit
        ) {
            this.setCurrentBaseRate = setCurrentBaseRate
            this.pauseTimer = pauseTimer
            this.resumeTimer = resumeTimer
            currencyValue.setText(rateUI.amount)
            currencyValue.onFocusChangeListener = currencyValueFocusListener
            currencyValue.setOnEditorActionListener(currencyValueEditorAction)
            currencyValue.addTextChangedListener(textWatcher)

            currencyCode.text = rateUI.currencyCode

            itemView.setOnClickListener {
                pauseTimer()

                updateCurrentBaseRate(rates[layoutPosition])
                swapSelectedItemAndFirst()

                resumeTimer()
            }

            currencyName.text = currencies.codeToName[rateUI.currencyCode]

            val context = itemView.context
            val resources = context.resources
            val flagFileName = currencies.codeToFlagImage[rateUI.currencyCode]
            val flagImageId = resources.getIdentifier(flagFileName, "drawable", context.packageName)
            currencyFlag.setImageDrawable(resources.getDrawable(flagImageId))
        }
    }
}

