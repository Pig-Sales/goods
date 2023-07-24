package com.ps.controller;

import com.ps.pojo.Days;
import com.ps.pojo.Result;
import com.ps.pojo.Today_Price;
import com.ps.service.TodayPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/goods")
public class TodayPriceController {

    @Autowired
    private TodayPriceService todayPriceService;

    @PostMapping("/getTodayPrice")
    public Result getTodayPrice(@RequestBody Days days){
        List<Today_Price> today_prices= todayPriceService.getTodayPrice(days.getDays());
        return Result.success(today_prices);
    }

    @PostMapping("/createTodayPrice")
    public Result createTodayPrice(@RequestBody Today_Price todayPrice){
        Today_Price todayPrice1 = todayPriceService.createTodayPrice(todayPrice.getToday_price(),todayPrice.getToday_price_time());
        return Result.success(todayPrice1);
    }
}
