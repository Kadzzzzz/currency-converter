package com.example.currency_converter;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CurrencyConverter";

    private TextView tvExchangeRate, tvLastUpdate;
    private TextView tvFromFlag, tvFromCurrency, tvToFlag, tvToCurrency, tvToAmount;
    private EditText etFromAmount;
    private LinearLayout layoutFromCurrency, layoutToCurrency;
    private Button btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
    private Button btnDot, btnDelete, btnSwap;

    private List<Currency> currencies;
    private Currency selectedFromCurrency;
    private Currency selectedToCurrency;
    private Map<String, Double> exchangeRates;
    private String currentInput = "0";
    private DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
    private ExchangeRateApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Appliquer les paramètres sauvegardés
        SettingsActivity.applySettings(this);

        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Starting application");

        initViews();
        initCurrencies();
        initApi();
        loadExchangeRates();
        setupListeners();
    }

    private void initViews() {
        Log.d(TAG, "initViews: Initializing views");

        tvExchangeRate = findViewById(R.id.tvExchangeRate);
        tvLastUpdate = findViewById(R.id.tvLastUpdate);
        tvFromFlag = findViewById(R.id.tvFromFlag);
        tvFromCurrency = findViewById(R.id.tvFromCurrency);
        tvToFlag = findViewById(R.id.tvToFlag);
        tvToCurrency = findViewById(R.id.tvToCurrency);
        tvToAmount = findViewById(R.id.tvToAmount);
        etFromAmount = findViewById(R.id.etFromAmount);
        layoutFromCurrency = findViewById(R.id.layoutFromCurrency);
        layoutToCurrency = findViewById(R.id.layoutToCurrency);

        btn0 = findViewById(R.id.btn0);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);
        btn7 = findViewById(R.id.btn7);
        btn8 = findViewById(R.id.btn8);
        btn9 = findViewById(R.id.btn9);
        btnDot = findViewById(R.id.btnDot);
        btnDelete = findViewById(R.id.btnDelete);
        btnSwap = findViewById(R.id.btnSwap);

        etFromAmount.setText(currentInput);
        tvToAmount.setText("0");

        Log.d(TAG, "initViews: Views initialized successfully");
    }

    private void initCurrencies() {
        Log.d(TAG, "initCurrencies: Initializing currencies");

        currencies = new ArrayList<>();
        currencies.add(new Currency("USD", getString(R.string.usd), "🇺🇸"));
        currencies.add(new Currency("EUR", getString(R.string.eur), "🇪🇺"));
        currencies.add(new Currency("GBP", getString(R.string.gbp), "🇬🇧"));
        currencies.add(new Currency("JPY", getString(R.string.jpy), "🇯🇵"));
        currencies.add(new Currency("CAD", getString(R.string.cad), "🇨🇦"));
        currencies.add(new Currency("AUD", getString(R.string.aud), "🇦🇺"));
        currencies.add(new Currency("CHF", getString(R.string.chf), "🇨🇭"));
        currencies.add(new Currency("CNY", getString(R.string.cny), "🇨🇳"));

        selectedFromCurrency = currencies.get(0);
        selectedToCurrency = currencies.get(1);

        Log.d(TAG, "initCurrencies: From=" + selectedFromCurrency.getCode() +
                ", To=" + selectedToCurrency.getCode());

        updateCurrencyDisplay();
    }

    private void initApi() {
        Log.d(TAG, "initApi: Setting up Retrofit");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://open.er-api.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ExchangeRateApi.class);

        Log.d(TAG, "initApi: Retrofit ready");
    }

    private void loadExchangeRates() {
        Log.d(TAG, "loadExchangeRates: Loading rates for " + selectedFromCurrency.getCode());

        tvLastUpdate.setText(R.string.error_loading);

        api.getExchangeRates(selectedFromCurrency.getCode()).enqueue(new Callback<ExchangeRateResponse>() {
            @Override
            public void onResponse(Call<ExchangeRateResponse> call, Response<ExchangeRateResponse> response) {
                Log.d(TAG, "onResponse: Received response, success=" + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    exchangeRates = response.body().getConversionRates();

                    Log.d(TAG, "onResponse: Rates loaded, size=" +
                            (exchangeRates != null ? exchangeRates.size() : "null"));

                    if (exchangeRates != null && exchangeRates.containsKey(selectedToCurrency.getCode())) {
                        Double rate = exchangeRates.get(selectedToCurrency.getCode());
                        Log.d(TAG, "onResponse: Rate for " + selectedToCurrency.getCode() + " = " + rate);
                    }

                    updateExchangeRateDisplay();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    String updateTime = sdf.format(new Date(response.body().getTimeLastUpdateUnix() * 1000));
                    tvLastUpdate.setText(getString(R.string.last_update, updateTime));

                    performConversion();
                } else {
                    Log.e(TAG, "onResponse: Failed - " + response.code());
                    showError(getString(R.string.error_network));
                }
            }

            @Override
            public void onFailure(Call<ExchangeRateResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage(), t);
                showError(getString(R.string.error_network) + ": " + t.getMessage());
            }
        });
    }

    private void setupListeners() {
        Log.d(TAG, "setupListeners: Setting up button listeners");

        View.OnClickListener numberClickListener = v -> {
            Button btn = (Button) v;
            appendToInput(btn.getText().toString());
        };

        btn0.setOnClickListener(numberClickListener);
        btn1.setOnClickListener(numberClickListener);
        btn2.setOnClickListener(numberClickListener);
        btn3.setOnClickListener(numberClickListener);
        btn4.setOnClickListener(numberClickListener);
        btn5.setOnClickListener(numberClickListener);
        btn6.setOnClickListener(numberClickListener);
        btn7.setOnClickListener(numberClickListener);
        btn8.setOnClickListener(numberClickListener);
        btn9.setOnClickListener(numberClickListener);
        btnDot.setOnClickListener(numberClickListener);

        btnDelete.setOnClickListener(v -> deleteLastCharacter());
        btnSwap.setOnClickListener(v -> swapCurrencies());

        layoutFromCurrency.setOnClickListener(v -> showCurrencyPicker(true));
        layoutToCurrency.setOnClickListener(v -> showCurrencyPicker(false));

        // AJOUT : Bouton Paramètres
        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void appendToInput(String value) {
        Log.d(TAG, "appendToInput: " + value + ", current=" + currentInput);

        if (value.equals(".") && currentInput.contains(".")) {
            Log.d(TAG, "appendToInput: Decimal point already exists");
            return;
        }

        if (value.equals(",") && currentInput.contains(",")) {
            Log.d(TAG, "appendToInput: Decimal point already exists");
            return;
        }

        // Remplacer la virgule par un point pour les calculs
        if (value.equals(",")) {
            value = ".";
        }

        if (currentInput.equals("0") && !value.equals(".")) {
            currentInput = value;
        } else {
            currentInput += value;
        }

        Log.d(TAG, "appendToInput: New input=" + currentInput);

        etFromAmount.setText(currentInput);
        performConversion();
    }

    private void deleteLastCharacter() {
        Log.d(TAG, "deleteLastCharacter: current=" + currentInput);

        if (!currentInput.isEmpty() && currentInput.length() > 1) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
        } else {
            currentInput = "0";
        }

        Log.d(TAG, "deleteLastCharacter: new=" + currentInput);

        etFromAmount.setText(currentInput);
        performConversion();
    }

    private void performConversion() {
        Log.d(TAG, "performConversion: Starting conversion");
        Log.d(TAG, "performConversion: Input=" + currentInput);
        Log.d(TAG, "performConversion: Rates null? " + (exchangeRates == null));

        if (exchangeRates == null) {
            Log.w(TAG, "performConversion: Exchange rates not loaded yet");
            tvToAmount.setText("0");
            return;
        }

        try {
            double amount = Double.parseDouble(currentInput);
            Log.d(TAG, "performConversion: Parsed amount=" + amount);

            Double rate = exchangeRates.get(selectedToCurrency.getCode());
            Log.d(TAG, "performConversion: Rate for " + selectedToCurrency.getCode() + " = " + rate);

            if (rate != null) {
                double result = amount * rate;
                String formattedResult = decimalFormat.format(result);

                Log.d(TAG, "performConversion: Result=" + result + ", formatted=" + formattedResult);

                tvToAmount.setText(formattedResult);

                Log.d(TAG, "performConversion: Display updated to: " + formattedResult);
            } else {
                Log.e(TAG, "performConversion: Rate is null for " + selectedToCurrency.getCode());
                tvToAmount.setText("0");
                showError(getString(R.string.error_conversion));
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "performConversion: Parse error", e);
            tvToAmount.setText("0");
        }
    }

    private void swapCurrencies() {
        Log.d(TAG, "swapCurrencies: Swapping currencies");

        Currency temp = selectedFromCurrency;
        selectedFromCurrency = selectedToCurrency;
        selectedToCurrency = temp;

        Log.d(TAG, "swapCurrencies: New From=" + selectedFromCurrency.getCode() +
                ", To=" + selectedToCurrency.getCode());

        updateCurrencyDisplay();
        loadExchangeRates();
    }

    private void updateCurrencyDisplay() {
        Log.d(TAG, "updateCurrencyDisplay: Updating display");

        tvFromFlag.setText(selectedFromCurrency.getFlag());
        tvFromCurrency.setText(selectedFromCurrency.getName());
        tvToFlag.setText(selectedToCurrency.getFlag());
        tvToCurrency.setText(selectedToCurrency.getName());
        updateExchangeRateDisplay();
    }

    private void updateExchangeRateDisplay() {
        Log.d(TAG, "updateExchangeRateDisplay: Updating rate display");

        if (exchangeRates != null) {
            Double rate = exchangeRates.get(selectedToCurrency.getCode());
            if (rate != null) {
                String rateText = "1 " + selectedFromCurrency.getCode() + " = " +
                        decimalFormat.format(rate) + " " + selectedToCurrency.getCode();
                tvExchangeRate.setText(rateText);
                Log.d(TAG, "updateExchangeRateDisplay: " + rateText);
            } else {
                Log.w(TAG, "updateExchangeRateDisplay: Rate is null");
            }
        } else {
            Log.w(TAG, "updateExchangeRateDisplay: exchangeRates is null");
        }
    }

    private void showCurrencyPicker(boolean isFromCurrency) {
        Log.d(TAG, "showCurrencyPicker: isFrom=" + isFromCurrency);

        String[] currencyNames = new String[currencies.size()];
        for (int i = 0; i < currencies.size(); i++) {
            currencyNames[i] = currencies.get(i).toString();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isFromCurrency ? "Source" : "Destination");
        builder.setItems(currencyNames, (dialog, which) -> {
            if (isFromCurrency) {
                selectedFromCurrency = currencies.get(which);
                Log.d(TAG, "Currency changed: From=" + selectedFromCurrency.getCode());
            } else {
                selectedToCurrency = currencies.get(which);
                Log.d(TAG, "Currency changed: To=" + selectedToCurrency.getCode());
            }
            updateCurrencyDisplay();
            loadExchangeRates();
        });
        builder.show();
    }

    private void showError(String message) {
        Log.e(TAG, "showError: " + message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        tvLastUpdate.setText(message);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentInput", currentInput);
        outState.putString("fromCode", selectedFromCurrency.getCode());
        outState.putString("toCode", selectedToCurrency.getCode());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentInput = savedInstanceState.getString("currentInput", "0");
        etFromAmount.setText(currentInput);

        String fromCode = savedInstanceState.getString("fromCode");
        String toCode = savedInstanceState.getString("toCode");

        for (Currency currency : currencies) {
            if (currency.getCode().equals(fromCode)) {
                selectedFromCurrency = currency;
            }
            if (currency.getCode().equals(toCode)) {
                selectedToCurrency = currency;
            }
        }
        updateCurrencyDisplay();
    }
}