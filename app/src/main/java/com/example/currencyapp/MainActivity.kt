package com.example.currencyapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var editTextFrom: EditText
    private lateinit var editTextTo: EditText
    private lateinit var spinnerFrom: Spinner
    private lateinit var spinnerTo: Spinner
    private lateinit var textViewRate: android.widget.TextView

    // Danh sách tiền tệ và tỷ giá cố định (1 USD làm chuẩn)
    private val currencies = listOf(
        "United States - Dollar (USD)",
        "Vietnam - Dong (VND)",
        "Euro - Euro (EUR)",
        "Japan - Yen (JPY)",
        "United Kingdom - Pound (GBP)",
        "Australia - Dollar (AUD)",
        "Canada - Dollar (CAD)",
        "China - Yuan (CNY)",
        "India - Rupee (INR)",
        "South Korea - Won (KRW)"
    )

    private val exchangeRates = mapOf(
        "USD" to 1.0,
        "VND" to 23185.0, // 1 USD = 23,185 VND
        "EUR" to 0.92,    // 1 USD = 0.92 EUR
        "JPY" to 149.50,  // 1 USD = 149.50 JPY
        "GBP" to 0.77,    // 1 USD = 0.77 GBP
        "AUD" to 1.50,    // 1 USD = 1.50 AUD
        "CAD" to 1.38,    // 1 USD = 1.38 CAD
        "CNY" to 7.12,    // 1 USD = 7.12 CNY
        "INR" to 83.95,   // 1 USD = 83.95 INR
        "KRW" to 1380.0   // 1 USD = 1380 KRW
    )

    private var activeEditText: EditText? = null // Theo dõi EditText đang được nhập
    private var isUpdating = false // Cờ để ngăn vòng lặp trong TextWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo các thành phần giao diện
        editTextFrom = findViewById(R.id.editTextFrom)
        editTextTo = findViewById(R.id.editTextTo)
        spinnerFrom = findViewById(R.id.spinnerFrom)
        spinnerTo = findViewById(R.id.spinnerTo)
        textViewRate = findViewById(R.id.textViewRate)

        // Thiết lập Spinner với danh sách tiền tệ
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFrom.adapter = adapter
        spinnerTo.adapter = adapter

        // Mặc định chọn USD và VND
        spinnerFrom.setSelection(0) // USD
        spinnerTo.setSelection(1)   // VND

        // Xử lý khi người dùng chọn EditText
        editTextFrom.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) activeEditText = editTextFrom
        }
        editTextTo.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) activeEditText = editTextTo
        }

        // Xử lý khi người dùng nhập giá trị vào EditText
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isUpdating) { // Chỉ gọi convertCurrency nếu không đang cập nhật
                    convertCurrency()
                }
            }
        }
        editTextFrom.addTextChangedListener(textWatcher)
        editTextTo.addTextChangedListener(textWatcher)

        // Xử lý khi người dùng thay đổi tiền tệ trong Spinner
        val spinnerListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                convertCurrency()
                updateExchangeRateText()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerFrom.onItemSelectedListener = spinnerListener
        spinnerTo.onItemSelectedListener = spinnerListener
    }

    // Hàm chuyển đổi tiền tệ
    private fun convertCurrency() {
        if (activeEditText == null) return // Tránh lỗi nếu chưa chọn EditText

        val fromCurrency = spinnerFrom.selectedItem.toString().substringAfterLast("(").substringBefore(")")
        val toCurrency = spinnerTo.selectedItem.toString().substringAfterLast("(").substringBefore(")")

        val fromRate = exchangeRates[fromCurrency] ?: 1.0
        val toRate = exchangeRates[toCurrency] ?: 1.0

        val amount: Double = try {
            if (activeEditText == editTextFrom) {
                editTextFrom.text.toString().toDouble()
            } else {
                editTextTo.text.toString().toDouble()
            }
        } catch (e: NumberFormatException) {
            0.0
        }

        val result: Double = if (activeEditText == editTextFrom) {
            (amount / fromRate) * toRate
        } else {
            (amount / toRate) * fromRate
        }

        // Định dạng kết quả với tối đa 2 chữ số thập phân (trừ VND, JPY, KRW không có thập phân)
        val formatter = DecimalFormat("#,##0.##")
        if (toCurrency == "VND" || toCurrency == "JPY" || toCurrency == "KRW") {
            formatter.maximumFractionDigits = 0
        } else {
            formatter.minimumFractionDigits = 2
        }

        // Cập nhật EditText không được chọn
        isUpdating = true // Đặt cờ để ngăn TextWatcher
        if (activeEditText == editTextFrom) {
            editTextTo.setText(formatter.format(result))
        } else {
            editTextFrom.setText(formatter.format(result))
        }
        isUpdating = false // Bỏ cờ sau khi cập nhật
    }

    // Cập nhật dòng tỷ giá tham khảo
    private fun updateExchangeRateText() {
        val fromCurrency = spinnerFrom.selectedItem.toString().substringAfterLast("(").substringBefore(")")
        val toCurrency = spinnerTo.selectedItem.toString().substringAfterLast("(").substringBefore(")")

        val fromRate = exchangeRates[fromCurrency] ?: 1.0
        val toRate = exchangeRates[toCurrency] ?: 1.0

        val rate = toRate / fromRate
        val formatter = DecimalFormat("#,##0.##")
        if (toCurrency == "VND" || toCurrency == "JPY" || toCurrency == "KRW") {
            formatter.maximumFractionDigits = 0
        } else {
            formatter.minimumFractionDigits = 2
        }

        textViewRate.text = "1 $fromCurrency = ${formatter.format(rate)} $toCurrency"
    }
}