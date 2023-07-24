package com.ps.service;

import com.ps.pojo.Today_Price;

import java.util.List;

public interface TodayPriceService {
    List<Today_Price> getTodayPrice(Integer days);

    Today_Price createTodayPrice(Float todayPrice, String todayPriceTime);
}
