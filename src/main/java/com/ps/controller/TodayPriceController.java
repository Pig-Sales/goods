package com.ps.controller;

import com.ps.pojo.Days;
import com.ps.pojo.Result;
import com.ps.pojo.Today_Price;
import com.ps.service.TodayPriceService;
import com.ps.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;


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
    public Result createTodayPrice(@RequestHeader String Authorization,@RequestBody Today_Price todayPrice){
        Claims claims = JwtUtils.parseJWT(Authorization);
        String user_auth = (String) claims.get("user_auth");
        if(!Objects.equals(user_auth, "admin")) {
            return Result.error("无平台方权限");
        }
        Today_Price todayPrice1 = todayPriceService.createTodayPrice(todayPrice.getToday_price(),todayPrice.getToday_price_time());
        return Result.success(todayPrice1);
    }
}
