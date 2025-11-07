package com.example.currency_converter;

import android.app.AlertDialog;
import android.os.Bundle;
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

    private TextView tvExchangeRate, tvLastUpdate;
    private TextView tvFromFlag, tvFromCurrency, tvToFlag, tvToCurrency, tvToAmount;
    private EditText etFromAmount;
    private LinearLayout layoutFromCurrency, layoutToCurrency;
    private Button btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
    private Button btnDot, btnDelete, btnSwap;

    private List<Currency> currencies;
    private Currency fromCurrency;
    private Currency toCurrency;
    private Map<String, Double> rates;
    private String input = "0";
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    private ExchangeRateApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsActivity.applySettings(this);
        setContentView(R.layout.activity_main);

        initViews();
        initCurrencies();
        initApi();
        loadRates();
        setupListeners();
    }

    private void initViews() {
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

        etFromAmount.setText(input);
        tvToAmount.setText("0");
    }

    private void initCurrencies() {
        currencies = new ArrayList<>();
        currencies.add(new Currency("USD", getString(R.string.usd), "🇺🇸"));
        currencies.add(new Currency("EUR", getString(R.string.eur), "🇪🇺"));
        currencies.add(new Currency("GBP", getString(R.string.gbp), "🇬🇧"));
        currencies.add(new Currency("JPY", getString(R.string.jpy), "🇯🇵"));
        currencies.add(new Currency("CAD", getString(R.string.cad), "🇨🇦"));
        currencies.add(new Currency("AUD", getString(R.string.aud), "🇦🇺"));
        currencies.add(new Currency("CHF", getString(R.string.chf), "🇨🇭"));
        currencies.add(new Currency("CNY", getString(R.string.cny), "🇨🇳"));

        fromCurrency = currencies.get(0);
        toCurrency = currencies.get(1);
        updateDisplay();
    }

    private void initApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://open.er-api.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ExchangeRateApi.class);
    }

    private void loadRates() {
        tvLastUpdate.setText(R.string.error_loading);

        api.getExchangeRates(fromCurrency.getCode()).enqueue(new Callback<ExchangeRateResponse>() {
            @Override
            public void onResponse(Call<ExchangeRateResponse> call, Response<ExchangeRateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rates = response.body().getRates();
                    updateRateDisplay();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    String time = sdf.format(new Date(response.body().getLastUpdate() * 1000));
                    tvLastUpdate.setText(getString(R.string.last_update, time));

                    convert();
                } else {
                    showError(getString(R.string.error_network));
                }
            }

            @Override
            public void onFailure(Call<ExchangeRateResponse> call, Throwable t) {
                showError(getString(R.string.error_network));
            }
        });
    }

    private void setupListeners() {
        View.OnClickListener numberClick = v -> {
            Button btn = (Button) v;
            appendInput(btn.getText().toString());
        };

        btn0.setOnClickListener(numberClick);
        btn1.setOnClickListener(numberClick);
        btn2.setOnClickListener(numberClick);
        btn3.setOnClickListener(numberClick);
        btn4.setOnClickListener(numberClick);
        btn5.setOnClickListener(numberClick);
        btn6.setOnClickListener(numberClick);
        btn7.setOnClickListener(numberClick);
        btn8.setOnClickListener(numberClick);
        btn9.setOnClickListener(numberClick);
        btnDot.setOnClickListener(numberClick);

        btnDelete.setOnClickListener(v -> deleteChar());
        btnSwap.setOnClickListener(v -> swap());

        layoutFromCurrency.setOnClickListener(v -> pickCurrency(true));
        layoutToCurrency.setOnClickListener(v -> pickCurrency(false));

        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });
    }

    private void appendInput(String value) {
        if (value.equals(".") && input.contains(".")) {
            return;
        }

        if (value.equals(",") && input.contains(",")) {
            return;
        }

        if (value.equals(",")) {
            value = ".";
        }

        if (input.equals("0") && !value.equals(".")) {
            input = value;
        } else {
            input += value;
        }

        etFromAmount.setText(input);
        convert();
    }

    private void deleteChar() {
        if (input.length() > 1) {
            input = input.substring(0, input.length() - 1);
        } else {
            input = "0";
        }

        etFromAmount.setText(input);
        convert();
    }

    private void convert() {
        if (rates == null) {
            tvToAmount.setText("0");
            return;
        }

        try {
            double amount = Double.parseDouble(input);
            Double rate = rates.get(toCurrency.getCode());

            if (rate != null) {
                tvToAmount.setText(df.format(amount * rate));
            } else {
                tvToAmount.setText("0");
                showError(getString(R.string.error_conversion));
            }
        } catch (NumberFormatException e) {
            tvToAmount.setText("0");
        }
    }

    private void swap() {
        Currency temp = fromCurrency;
        fromCurrency = toCurrency;
        toCurrency = temp;

        updateDisplay();
        loadRates();
    }

    private void updateDisplay() {
        tvFromFlag.setText(fromCurrency.getFlag());
        tvFromCurrency.setText(fromCurrency.getName());
        tvToFlag.setText(toCurrency.getFlag());
        tvToCurrency.setText(toCurrency.getName());
        updateRateDisplay();
    }

    private void updateRateDisplay() {
        if (rates != null) {
            Double rate = rates.get(toCurrency.getCode());
            if (rate != null) {
                String text = "1 " + fromCurrency.getCode() + " = " +
                        df.format(rate) + " " + toCurrency.getCode();
                tvExchangeRate.setText(text);
            }
        }
    }

    private void pickCurrency(boolean isFrom) {
        String[] names = new String[currencies.size()];
        for (int i = 0; i < currencies.size(); i++) {
            names[i] = currencies.get(i).toString();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isFrom ? "Source" : "Destination");
        builder.setItems(names, (dialog, which) -> {
            if (isFrom) {
                fromCurrency = currencies.get(which);
            } else {
                toCurrency = currencies.get(which);
            }
            updateDisplay();
            loadRates();
        });
        builder.show();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        tvLastUpdate.setText(message);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("input", input);
        outState.putString("from", fromCurrency.getCode());
        outState.putString("to", toCurrency.getCode());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        input = savedInstanceState.getString("input", "0");
        etFromAmount.setText(input);

        String from = savedInstanceState.getString("from");
        String to = savedInstanceState.getString("to");

        for (Currency c : currencies) {
            if (c.getCode().equals(from)) fromCurrency = c;
            if (c.getCode().equals(to)) toCurrency = c;
        }
        updateDisplay();
    }
}