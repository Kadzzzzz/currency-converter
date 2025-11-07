package com.example.currency_converter;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ExchangeRateResponse {
    @SerializedName("result")
    private String result;

    @SerializedName("base_code")
    private String base;

    @SerializedName("rates")
    private Map<String, Double> rates;

    @SerializedName("time_last_update_unix")
    private long lastUpdate;

    public String getResult() {
        return result;
    }

    public String getBase() {
        return base;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }
}