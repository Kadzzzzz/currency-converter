package com.example.currency_converter;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ExchangeRateApi {
    @GET("v6/latest/{base}")
    Call<ExchangeRateResponse> getExchangeRates(@Path("base") String baseCurrency);
}