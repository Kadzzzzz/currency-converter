package com.example.currency_converter;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ExchangeRateResponse {
    @SerializedName("result")
    private String result;

    @SerializedName("base_code")
    private String baseCode;

    @SerializedName("rates")
    private Map<String, Double> rates;

    @SerializedName("time_last_update_unix")
    private long timeLastUpdateUnix;

    public String getResult() {
        return result;
    }

    public String getBaseCode() {
        return baseCode;
    }

    public Map<String, Double> getConversionRates() {
        return rates;
    }

    public long getTimeLastUpdateUnix() {
        return timeLastUpdateUnix;
    }
}