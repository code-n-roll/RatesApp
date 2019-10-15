package com.karanchuk.ratesapp.presentation.rates

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.karanchuk.ratesapp.R
import com.karanchuk.ratesapp.data.Currencies
import com.karanchuk.ratesapp.domain.common.Utils
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.android.synthetic.main.item_rate.view.*
import java.util.*
import kotlin.collections.ArrayList


class RatesListAdapter(
    private val setCurrentBaseRate: (RateUI) -> Unit,
    private val currencies: Currencies
) : RecyclerView.Adapter<RatesListAdapter.RateViewHolder>() {

    companion object {
        private const val CLICK_ACTION_THRESHOLD = 300
    }

    private val rates: ArrayList<RateUI> = ArrayList()
    private val savedRates = ArrayList<RateUI>()

    private fun notifyItemsChangedWithPayload() {
        rates.drop(1).forEachIndexed { i, rate ->
            notifyItemChanged(i+1, rate.amount)
        }
    }

    fun updateRates(rates: List<RateUI>) {
        if (this.rates.isEmpty()) {
            this.rates.clear()
            this.rates.addAll(rates)
        } else {
            this.rates.forEach { rateUI ->
                val newRate = rates.find { it.currencyCode == rateUI.currencyCode}
                if (newRate != null) {
                    rateUI.amount = newRate.amount
                }
            }
        }
        notifyItemsChangedWithPayload()
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

    override fun onBindViewHolder(
        holder: RateViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        when {
            payloads.isEmpty() -> holder.bind(rates[position])
            else -> holder.setCurrencyAmount(payloads[0] as String)
        }
    }

    inner class RateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var lastTouchDown: Long = 0

        private val currencyFlag: RoundedImageView = itemView.currency_flag
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
                if (layoutPosition == 0 && itemView.currency_value.hasFocus()) {
                    val text = p0.toString()
                    val baseRateAmount = if (
                        text.isEmpty()
                        || text.toDoubleOrNull() == null
                        || text.toDoubleOrNull() == .0
                    ) {
                        ""
                    } else {
                        text
                    }

                    if (rates[0].amount.isNotEmpty() && baseRateAmount.isEmpty()) {
                        savedRates.clear()
                        rates.forEach { rate ->
                            savedRates.add(RateUI(rate.amount, rate.currencyCode, rate.currencyName))
                        }

                        rates.forEachIndexed { i, rateUI ->
                            val savedRate = savedRates.find { it.currencyCode == rateUI.currencyCode}
                            if (savedRate != null) {
                                rateUI.currencyCode = savedRate.currencyCode
                                rateUI.currencyName = savedRate.currencyName
                            }
                            rateUI.amount = ""
                        }
                        updateCurrentBaseRate(baseRateAmount)
                        notifyItemsChangedWithPayload()
                        return
                    } else if (rates[0].amount.isEmpty() && baseRateAmount.isNotEmpty()) {
                        rates.forEach { rateUI ->
                            val savedRate = savedRates.find { it.currencyCode == rateUI.currencyCode}
                            if (savedRate != null) {
                                rateUI.currencyCode = savedRate.currencyCode
                                rateUI.currencyName = savedRate.currencyName
                                rateUI.amount = savedRate.amount
                            }
                        }
                    } else if (rates[0].amount.isEmpty() && baseRateAmount.isEmpty()) {
                        return
                    }

                    Utils.convertRatesBy(rates, baseRateAmount.toDouble() / rates[0].amount.toDouble())
                    Utils.roundRateAmounts(rates)

                    updateCurrentBaseRate(baseRateAmount)
                    notifyItemsChangedWithPayload()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // nop
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // nop
            }
        }

        private val currencyValueOnFocusChange = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                updateCurrentBaseRate(rates[layoutPosition])
                swapSelectedItemAndFirst()
            } else {
                hideKeyboard()
            }
        }

        private val itemViewTouchListener = View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> lastTouchDown = System.currentTimeMillis()
                MotionEvent.ACTION_UP -> if (System.currentTimeMillis() - lastTouchDown < CLICK_ACTION_THRESHOLD) {
                    itemView.performClick()
                }
            }
            false
        }
        private val itemViewClickListener = View.OnClickListener {
            updateCurrentBaseRate(rates[layoutPosition])
            swapSelectedItemAndFirst()
        }

        private fun swapSelectedItemAndFirst() {
            layoutPosition.takeIf { it > 0 }?.also { currentPosition ->
                val ratesLinkedList = LinkedList(rates)
                ratesLinkedList.removeAt(currentPosition).also {
                    ratesLinkedList.addFirst(it)
                }
                rates.clear()
                rates.addAll(ratesLinkedList)
                notifyItemMoved(currentPosition, 0)
            }
            focusEditTextOnSwap()
        }

        private fun focusEditTextOnSwap() {
            val recycler = (itemView.parent as RecyclerView)
            recycler.post {
                recycler.layoutManager?.findViewByPosition(0)?.currency_value?.let {
                    it.requestFocus()
                    it.setSelection(it.text.length)
                    showKeyboard(it)
                }
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

        private fun hideKeyboard() {
            val imm = itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(itemView.windowToken, 0)
        }

        private fun showKeyboard(editText: EditText) {
            val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }

        private fun getCurrencyFlag(flagFileName: String): BitmapDrawable {
            val context = itemView.context
            val resources = context.resources
            val flagImageId = resources.getIdentifier(flagFileName, "drawable", context.packageName)
            val flagImageDrawable = resources.getDrawable(flagImageId)
            return BitmapDrawable(resources, Bitmap.createScaledBitmap((flagImageDrawable as BitmapDrawable).bitmap, 235, 235, true))
        }

        fun bind(rateUI: RateUI) {
            currencyValue.setText(rateUI.amount)
            currencyValue.onFocusChangeListener = currencyValueOnFocusChange
            currencyValue.setOnEditorActionListener(currencyValueEditorAction)
            currencyValue.addTextChangedListener(textWatcher)

            currencyCode.text = rateUI.currencyCode
            currencyName.text = currencies.codeToName[rateUI.currencyCode]

            val flagFileName = currencies.codeToFlagImage[rateUI.currencyCode] ?: ""
            currencyFlag.setImageDrawable(getCurrencyFlag(flagFileName))

            itemView.setOnTouchListener(itemViewTouchListener)
            itemView.setOnClickListener(itemViewClickListener)
        }

        fun setCurrencyAmount(amount: String) {
            currencyValue.setText(amount)
        }
    }
}

